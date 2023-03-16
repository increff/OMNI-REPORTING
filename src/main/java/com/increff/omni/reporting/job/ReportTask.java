package com.increff.omni.reporting.job;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.config.EmailProps;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.EmailUtil;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.persistence.OptimisticLockException;
import java.io.*;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
@Log4j
public class ReportTask {

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
    private ConnectionApi connectionApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private FileClient fileClient;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportScheduleApi reportScheduleApi;


    @Async("userReportRequestExecutor")
    public void runUserReportAsync(ReportRequestPojo pojo) throws ApiException, MessagingException {
        run(pojo);
    }

    @Async("scheduleReportRequestExecutor")
    public void runScheduleReportAsync(ReportRequestPojo pojo) throws ApiException, MessagingException {
        run(pojo);
    }

    private void run(ReportRequestPojo pojo) throws ApiException, MessagingException {
        // mark as processing - locking
        try {
            api.markProcessingIfEligible(pojo.getId());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
            log.debug("Error occurred while marking report in progress for request id : " + pojo.getId(), e);
            return;
        }
        // process
        try {
            ReportRequestPojo reportRequestPojo = api.getCheck(pojo.getId());

            List<ReportInputParamsPojo> reportInputParamsPojoList = reportInputParamsApi
                    .getInputParamsForReportRequest(reportRequestPojo.getId());
            ReportPojo reportPojo = reportApi.getCheck(reportRequestPojo.getReportId());
            ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
            OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(reportRequestPojo.getOrgId());
            ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());

            // Creation of file
            File file = folderApi.getFileForExtension(reportRequestPojo.getId(), ".tsv");
            File errorFile = folderApi.getErrFile(reportRequestPojo.getId(), ".tsv");
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            SqlParams sqlParams = CommonDtoHelper.convert(connectionPojo, file, errorFile
            );
            String fQuery = SqlCmd.prepareQuery(inputParamMap, reportQueryPojo.getQuery(),
                    properties.getMaxExecutionTime());
            sqlParams.setQuery(fQuery);
            // Execute query and save results
            saveResultsOnCloud(pojo, sqlParams);
            if(pojo.getType().equals(ReportRequestType.EMAIL)) {
                reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 1, 0);
            }
        } catch (Exception e) {
            log.error("Report Request ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED, e.getMessage(), 0, 0.0);
            try {
                if(pojo.getType().equals(ReportRequestType.EMAIL)) {
                    ReportSchedulePojo schedulePojo = reportScheduleApi.getCheck(pojo.getScheduleId());
                    List<String> toEmails = reportScheduleApi.getByScheduleId(schedulePojo.getId()).stream()
                            .map(ReportScheduleEmailsPojo::getSendTo).collect(
                                    Collectors.toList());
                    EmailProps props = createEmailProps(null, false, schedulePojo, toEmails, "Hi,<br>Please " +
                            "check failure reason in the latest scheduled requests. Re-submit the schedule in the " +
                            "reporting application, which might solve the issue.", false);
                    EmailUtil.sendMail(props);
                    reportScheduleApi.addScheduleCount(pojo.getScheduleId(), 0, 1);
                }
            } catch (Exception ex) {
                log.error("Report Request ID : " + pojo.getId() + ". Failed to send email. ", ex);
            }
        }
    }

    private void saveResultsOnCloud(ReportRequestPojo pojo, SqlParams sqlParams) throws IOException, ApiException {
        try {
            // Process data
            SqlCmd.processQuery(sqlParams, false, properties.getMaxExecutionTime());
            String name = sqlParams.getOutFile().getName().split(".tsv")[0] + ".csv";
            File csvFile = folderApi.getFile(name);
            Integer noOfRows = FileUtil.getCsvFromTsv(sqlParams.getOutFile(), csvFile);

            // upload result to cloud
            String filePath = "NA";
            double fileSize = FileUtil.getSizeInMb(csvFile.length());
            switch (pojo.getType()) {
                case EMAIL:
                    sendEmail(fileSize, csvFile, pojo);
                    break;
                case USER:
                    filePath = uploadFile(csvFile, pojo);
                    break;
            }
            // update status to completed
            api.updateStatus(pojo.getId(), ReportRequestStatus.COMPLETED, filePath, noOfRows, fileSize);
            FileUtil.delete(csvFile);
        } catch (ApiException apiException) {
            throw apiException;
        } catch (Exception e) {
            String message =
                    String.join("\t", Files.readAllLines(sqlParams.getErrFile().toPath())).concat(e.getMessage());
            throw new ApiException(ApiStatus.BAD_DATA, message);
        } finally {
            deleteFiles(sqlParams.getOutFile(), sqlParams.getErrFile());
        }
    }

    private void sendEmail(double fileSize, File csvFile, ReportRequestPojo pojo)
            throws IOException, ApiException, javax.mail.MessagingException {
        File out = csvFile;
        boolean isZip = false;
        if(fileSize > 50.0) {
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
        EmailProps props = createEmailProps(out, true, schedulePojo, toEmails, "", isZip);
        EmailUtil.sendMail(props);

    }

    private EmailProps createEmailProps(File out, Boolean isAttachment,
                                        ReportSchedulePojo schedulePojo, List<String> toEmails, String content,
                                        boolean isZip) {
        EmailProps props = new EmailProps();
        props.setFromEmail(properties.getFromEmail());
        props.setUsername(properties.getUsername());
        props.setPassword(properties.getPassword());
        props.setSmtpHost(properties.getSmtpHost());
        props.setSmtpPort(properties.getSmtpPort());
        props.setToEmails(toEmails);
        props.setSubject("Increff Reporting : " + schedulePojo.getReportName());
        props.setAttachment(out);
        props.setCustomizedFileName(schedulePojo.getReportName() + " - " + ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN)) + ((isZip) ? ".zip" : ".csv"));
        props.setIsAttachment(isAttachment);
        props.setContent(content);
        return props;
    }

    private void deleteFiles(File outFile, File errFile) {
        if (outFile.exists() && !FileUtil.delete(outFile))
            log.debug("File deletion failed, name : " + outFile.getName());
        if (errFile.exists() && !FileUtil.delete(errFile))
            log.debug("File deletion failed, name : " + errFile.getName());
    }

    private String uploadFile(File file, ReportRequestPojo pojo) throws FileNotFoundException, ApiException {
        InputStream inputStream = new FileInputStream(file);
        String filePath = pojo.getOrgId() + "/" + "REPORTS" + "/" + pojo.getId() + "_" + UUID.randomUUID() + ".csv";
        try {
            fileClient.create(filePath, inputStream);
        } catch (Exception e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error in uploading Report File to Gcp for report : " +
                    pojo.getId());
        }
        return filePath;
    }

    private Map<String, String> getInputParamMapFromPojoList(List<ReportInputParamsPojo> reportInputParamsPojoList) {
        Map<String, String> inputParamMap = new HashMap<>();
        reportInputParamsPojoList.forEach(r -> inputParamMap.put(r.getParamKey(), r.getParamValue()));
        return inputParamMap;
    }

}
