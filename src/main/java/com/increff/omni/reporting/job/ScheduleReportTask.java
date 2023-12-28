package com.increff.omni.reporting.job;

import com.increff.commons.queryexecutor.QueryExecutorClient;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.config.EmailProps;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.EmailUtil;
import com.increff.omni.reporting.util.FileDownloadUtil;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.increff.service.encryption.EncryptionClient;
import com.increff.service.encryption.common.CryptoCommon;
import com.increff.commons.springboot.client.AppClientException;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.persistence.OptimisticLockException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getInputParamMapFromPojoList;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getValueFromQuotes;

@Component
@Log4j
public class ScheduleReportTask extends AbstractTask {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportQueryApi reportQueryApi;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
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
    private QueryExecutorClient executorClient;
    @Autowired
    private EncryptionClient encryptionClient;

    private final static String TIME_ZONE_PATTERN_WITHOUT_ZONE = "yyyy-MM-dd HH:mm:ss";

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
            OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(reportRequestPojo.getOrgId());
            ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());

            // Creation of file
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            timezone = getValueFromQuotes(inputParamMap.get("timezone"));
            String fQuery = SqlCmd.getFinalQuery(inputParamMap, reportQueryPojo.getQuery(), false);
            // Execute query and save results
            prepareAndSendEmail(pojo, fQuery, connectionPojo, timezone, reportPojo.getName());
            reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 1, 0);
        } catch (Exception e) {
            log.error("Report Request ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED, e.getMessage(), 0, 0.0);
            try {
                ReportSchedulePojo schedulePojo = reportScheduleApi.getCheck(pojo.getScheduleId());
                List<String> toEmails = reportScheduleApi.getByScheduleId(schedulePojo.getId()).stream()
                        .map(ReportScheduleEmailsPojo::getSendTo).collect(
                                Collectors.toList());
                EmailProps props = createEmailProps(null, false, toEmails, "Hi,<br>Please " +
                        "check failure reason in the latest scheduled requests. Re-submit the schedule in the " +
                        "reporting application, which might solve the issue.", false, timezone, reportPojo.getName());
                EmailUtil.sendMail(props);
                reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 0, 1);
            } catch (Exception ex) {
                log.error("Report Request ID : " + pojo.getId() + ". Failed to send email. ", ex);
            }
        }

    }

    private void prepareAndSendEmail(ReportRequestPojo pojo, String fQuery, ConnectionPojo connectionPojo,
                                     String timezone, String name)
            throws IOException, ApiException {
        File file = folderApi.getFileForExtension(pojo.getId(), ".csv");
        Connection connection = null;
        ResultSet resultSet = null;
        String password = getDecryptedPassword(connectionPojo.getPassword());
        try {
            // Process data
            connection = dbConnectionApi.getConnection(connectionPojo.getHost(), connectionPojo.getUsername(),
                    password, properties.getMaxConnectionTime());
            PreparedStatement statement =
                    dbConnectionApi.getStatement(connection, properties.getMaxExecutionTime(), fQuery,
                            properties.getResultSetFetchSize());
            resultSet = statement.executeQuery();
            Integer noOfRows = FileUtil.writeCsvFromResultSet(resultSet, file);
            double fileSize = FileUtil.getSizeInMb(file.length());
            if (fileSize > properties.getMaxFileSize())
                throw new ApiException(ApiStatus.BAD_DATA,
                        "File size " + fileSize + " MB exceeded max limit of " + properties.getMaxFileSize() +
                                " MB" + ". Please select granular filters");
            String filePath = "NA";
            sendEmail(fileSize, file, pojo, timezone, name);
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

    private String getDecryptedPassword(String password) throws ApiException {
        try {
            CryptoCommon form = CommonDtoHelper.convertToCryptoForm(password);
            String decryptedPassword = encryptionClient.decode(form).getValue();
            return Objects.isNull(decryptedPassword) ? password : decryptedPassword;
        } catch (AppClientException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error From Crypto Service " + e.getMessage());
        }
    }

    private void sendEmail(double fileSize, File csvFile, ReportRequestPojo pojo, String timezone,
                           String name)
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
                name);
        EmailUtil.sendMail(props);
    }

    private EmailProps createEmailProps(File out, Boolean isAttachment,
                                        List<String> toEmails, String content,
                                        boolean isZip, String timezone, String name) {
        EmailProps props = new EmailProps();
        props.setFromEmail(properties.getFromEmail());
        props.setUsername(properties.getUsername());
        props.setPassword(properties.getPassword());
        props.setSmtpHost(properties.getSmtpHost());
        props.setSmtpPort(properties.getSmtpPort());
        props.setToEmails(toEmails);
        props.setSubject("Increff Reporting : " + name);
        props.setAttachment(out);
        props.setCustomizedFileName(name + " - " + ZonedDateTime.now().withZoneSameInstant(
                ZoneId.of(timezone)).format(DateTimeFormatter.ofPattern(TIME_ZONE_PATTERN_WITHOUT_ZONE))
                + ((isZip) ? ".zip" :
                ".csv"));
        props.setIsAttachment(isAttachment);
        props.setContent(content);
        return props;
    }
}
