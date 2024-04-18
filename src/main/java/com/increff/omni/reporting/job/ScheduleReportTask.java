package com.increff.omni.reporting.job;

import com.increff.commons.fileclient.AbstractFileProvider;
import com.increff.commons.fileclient.AwsFileProvider;
import com.increff.commons.fileclient.GcpFileProvider;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.config.EmailProps;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.DBType;
import com.increff.omni.reporting.model.constants.PipelineType;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.form.FileProviderFolder.AwsPipelineConfigForm;
import com.increff.omni.reporting.model.form.FileProviderFolder.GcpPipelineConfigForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.*;
import com.increff.service.encryption.EncryptionClient;
import com.increff.service.encryption.form.CryptoDecodeFormWithoutKey;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getInputParamMapFromPojoList;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getValueFromQuotes;
import static com.increff.omni.reporting.util.ConvertUtil.getJavaObjectFromJson;

@Component
@Log4j
public class ScheduleReportTask extends AbstractTask {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportQueryApi reportQueryApi;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;
    @Autowired
    private DBConnectionApi dbConnectionApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private FileDownloadUtil fileDownloadUtil;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportScheduleApi reportScheduleApi;
    @Autowired
    private SchedulePipelineApi schedulePipelineApi;
    @Autowired
    private PipelineApi pipelineApi;
    @Autowired
    private EncryptionClient encryptionClient;


    @Override
    @Async("scheduleReportRequestExecutor")
    protected void runReportAsync(ReportRequestPojo pojo) throws ApiException {
        // mark as processing - locking
        try {
            api.markProcessingIfEligible(pojo.getId());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
            log.debug("Error occurred while marking report in progress for request id : " + pojo.getId(), e);
            return;
        }
        // process
        String timezone = "Asia/Kolkata";
        ReportRequestPojo reportRequestPojo = api.getCheck(pojo.getId());
        ReportPojo reportPojo = reportApi.getCheck(reportRequestPojo.getReportId());
        try {
            List<ReportInputParamsPojo> reportInputParamsPojoList = reportInputParamsApi
                    .getInputParamsForReportRequest(reportRequestPojo.getId());
            ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
            OrgMappingPojo orgMappingPojo = orgMappingApi.getCheckByOrgIdSchemaVersionId(reportRequestPojo.getOrgId(),
                    reportPojo.getSchemaVersionId());
            ConnectionPojo connectionPojo = connectionApi.getCheck(orgMappingPojo.getConnectionId());

            // Creation of file
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            timezone = getValueFromQuotes(inputParamMap.get("timezone"));
            String fQuery = SqlCmd.getFinalQuery(inputParamMap, reportQueryPojo.getQuery(), false);
            // Execute query and save results
            prepareAndSendEmailOrPipelines(pojo, fQuery, connectionPojo, timezone, reportPojo);
            reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 1, 0);
        } catch (Exception e) {
            log.error("Report Request ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED, e.getMessage(), 0, 0.0);
            reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 0, 1);
            try {
                ReportSchedulePojo schedulePojo = reportScheduleApi.getCheck(pojo.getScheduleId());
                List<String> toEmails = reportScheduleApi.getByScheduleId(schedulePojo.getId()).stream()
                        .map(ReportScheduleEmailsPojo::getSendTo).collect(
                                Collectors.toList());
                if(!toEmails.isEmpty()) {
                    EmailProps props = createEmailProps(null, false, toEmails, "Hi,<br>Please " +
                            "check failure reason in the latest scheduled requests. Re-submit the schedule in the " +
                                    "reporting application, which might solve the issue.", false, timezone, reportPojo.getName(),
                            false, schedulePojo.getEmailSubject() + " : Failed To Execute");
                    EmailUtil.sendMail(props);
                }
            } catch (Exception ex) {
                log.error("Report Request ID : " + pojo.getId() + ". Failed to send email. ", ex);
            }
        }

    }

    private void prepareAndSendEmailOrPipelines(ReportRequestPojo pojo, String fQuery, ConnectionPojo connectionPojo,
                                                String timezone, ReportPojo reportPojo)
            throws IOException, ApiException {
        File file = folderApi.getFileForExtension(pojo.getId(), ".csv");
        Connection connection = null;
        ResultSet resultSet = null;
        int noOfRows = 0;
        String password = getDecryptedPassword(connectionPojo.getPassword());
        try {
            if(connectionPojo.getDbType().equals(DBType.MYSQL)) {
                connection = dbConnectionApi.getConnection(connectionPojo.getHost(), connectionPojo.getUsername(),
                        password, properties.getMaxConnectionTime());
                PreparedStatement statement =
                        dbConnectionApi.getStatement(connection, properties.getMaxExecutionTime(), fQuery,
                                properties.getResultSetFetchSize());
                resultSet = statement.executeQuery();
                noOfRows = FileUtil.writeCsvFromResultSet(resultSet, file);
            } else if (connectionPojo.getDbType().equals(DBType.MONGO)) {
                List<Document> docs = MongoUtil.executeMongoPipeline(connectionPojo.getHost(), connectionPojo.getUsername(),
                        password, fQuery);
                noOfRows = FileUtil.writeCsvFromMongoDocuments(docs, file);
            }

            double fileSize = FileUtil.getSizeInMb(file.length());
            if (fileSize > properties.getMaxFileSize())
                throw new ApiException(ApiStatus.BAD_DATA,
                        "File size " + fileSize + " MB exceeded max limit of " + properties.getMaxFileSize() +
                                " MB" + ". Please select granular filters");
            String filePath = "NA";

            boolean zeroRows = (noOfRows == 0);
            List<SchedulePipelinePojo> schedulePipelinePojos = schedulePipelineApi.getByScheduleId(pojo.getScheduleId());
            if(schedulePipelinePojos.isEmpty())
                sendEmail(fileSize, file, pojo, timezone, reportPojo.getName(), zeroRows);
            else {
                processPipelines(file, schedulePipelinePojos, FileUtil.getPipelineFilename(reportPojo.getId(), reportPojo.getName(), timezone));
            }

            // update status to completed
            api.updateStatus(pojo.getId(), ReportRequestStatus.COMPLETED, filePath, noOfRows, fileSize, "",
                    ZonedDateTime.now());
            FileUtil.delete(file);
        } catch (
                ApiException apiException) {
            throw apiException;
        } catch (SQLException sqlException) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Error while processing request : " + sqlException.getMessage());
        } catch (Throwable e) {
            throw new ApiException(ApiStatus.BAD_DATA, e.getMessage());
        } finally {
            try {
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
                if (Objects.nonNull(resultSet)) {
                    resultSet.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processPipelines(File file, List<SchedulePipelinePojo> schedulePipelinePojos, String filename) throws ApiException {
        List<PipelinePojo> pipelinePojos = pipelineApi.getByPipelineIds(schedulePipelinePojos.stream()
                .map(SchedulePipelinePojo::getPipelineId).collect(Collectors.toList()));
        Map<Integer, SchedulePipelinePojo> pipelineIdToSchedulePipelinePojoMap = schedulePipelinePojos.stream()
                .collect(Collectors.toMap(SchedulePipelinePojo::getPipelineId, x -> x));

        for (PipelinePojo pipelinePojo : pipelinePojos)
            uploadScheduleFiles(pipelinePojo.getType(), pipelinePojo.getConfigs().toString(), file,
                    pipelineIdToSchedulePipelinePojoMap.get(pipelinePojo.getId()).getFolderName(), filename);
    }

    public void uploadScheduleFiles(PipelineType type, String configs, File file, String folderName, String filename) throws ApiException {
        try {
            AbstractFileProvider fileProvider = getFileProvider(type, configs);
            fileProvider.create(getFilepathWithFolder(filename, folderName), Files.newInputStream(file.toPath()));
        } catch (Exception e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error while uploadScheduleFiles : " + e.getMessage());
        }
    }

    private static String getFilepathWithFolder(String filename, String folderName) {
        String filePath = filename;
        if (folderName != null && !folderName.isEmpty()) {
            filePath = folderName + "/" + filename;
        }
        return filePath;
    }

    private AbstractFileProvider getFileProvider(PipelineType type, String configs) throws ApiException {
        try {
            switch (type) {
                case AWS:
                    AwsPipelineConfigForm awsForm = getJavaObjectFromJson(configs, AwsPipelineConfigForm.class);
                    return new AwsFileProvider(awsForm.getRegion(), awsForm.getAccessKey(), awsForm.getSecretKey(), awsForm.getBucketName(), awsForm.getBucketUrl());
                case GCP:
                    GcpPipelineConfigForm gcpForm = getJavaObjectFromJson(configs, GcpPipelineConfigForm.class);
                    return new GcpFileProvider(gcpForm.getBucketUrl(), gcpForm.getBucketName(), new ByteArrayInputStream(gcpForm.getCredentialsJson().toString().getBytes()));
                default:
                    throw new ApiException(ApiStatus.BAD_DATA, "Unsupported File Provider Type " + type);
            }
        } catch (Exception e) {
            log.error("Error while getting file provider : " + e + " " + Arrays.toString(e.getStackTrace()));
            throw new ApiException(ApiStatus.BAD_DATA, "Error while getting file provider : " + e);
        }
    }



    private String getDecryptedPassword(String password) throws ApiException {
        try {
            CryptoDecodeFormWithoutKey form = CommonDtoHelper.convertToCryptoForm(password);
            String decryptedPassword = encryptionClient.decode(form).getValue();
            return Objects.isNull(decryptedPassword) ? password : decryptedPassword;
        } catch (AppClientException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error From Crypto Service " + e.getMessage());
        }
    }

    private void sendEmail(double fileSize, File csvFile, ReportRequestPojo pojo, String timezone,
                           String name, Boolean zeroRows)
            throws IOException, ApiException, javax.mail.MessagingException {
        File out = csvFile;
        boolean isZip = false;
        if (fileSize > 50.0) {
            throw new ApiException(ApiStatus.BAD_DATA, "File size has crossed 50 MB limit. Mail can't be sent");
        }
        if (fileSize > 15.0) {
            String outFileName = csvFile.getName().split(".csv")[0] + ".7z";
            File zipFile = folderApi.getFile(outFileName);
            try {
                // Create a 7z output stream
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos);
                ZipEntry ze = new ZipEntry(csvFile.getName());
                zos.putNextEntry(ze);

                // Add the input file to the archive
                zos.write(Files.readAllBytes(csvFile.toPath()));
                zos.finish();
                zos.close();
                fos.close();
            } catch (Exception e) {
                log.error("Error while zipping : ", e);
                throw new ApiException(ApiStatus.BAD_DATA, "Error while zipping the file");
            }
            out = zipFile;
            isZip = true;
        }
        ReportSchedulePojo schedulePojo = reportScheduleApi.getCheck(pojo.getScheduleId());
        List<String> toEmails = reportScheduleApi.getByScheduleId(schedulePojo.getId()).stream()
                .map(ReportScheduleEmailsPojo::getSendTo).collect(
                        Collectors.toList());
        EmailProps props = createEmailProps(out, true, toEmails, "", isZip, timezone,
                name, zeroRows, schedulePojo.getEmailSubject());
        EmailUtil.sendMail(props);
    }

    private EmailProps createEmailProps(File out, Boolean isAttachment,
                                        List<String> toEmails, String content,
                                        boolean isZip, String timezone, String name,
                                        Boolean zeroData, String customSubject) {
        EmailProps props = new EmailProps();
        props.setFromEmail(properties.getFromEmail());
        props.setUsername(properties.getUsername());
        props.setPassword(properties.getPassword());
        props.setSmtpHost(properties.getSmtpHost());
        props.setSmtpPort(properties.getSmtpPort());
        props.setToEmails(toEmails);

        String subject;
        if (Objects.nonNull(customSubject) && !customSubject.isEmpty())
            subject = customSubject;
        else
            subject = "Increff Reporting : " + name;

        if (Objects.nonNull(zeroData) && zeroData) // Add (No Data) to subject if zero data
            subject += " (No Data) ";
        props.setSubject(subject);

        props.setAttachment(out);
        props.setCustomizedFileName(FileUtil.getCustomizedFileName(isZip, timezone, name));
        props.setIsAttachment(isAttachment);
        props.setContent(content);
        return props;
    }
}
