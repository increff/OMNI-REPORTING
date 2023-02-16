package com.increff.omni.reporting.job;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.OptimisticLockException;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


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

    @Async("reportRequestExecutor")
    public void runUserReportAsync(ReportRequestPojo pojo) throws ApiException, IOException {
        // mark as processing - locking
        try {
            api.markProcessingIfEligible(pojo.getId());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
            log.debug("Error occurred while marking report in progress for request id : " + pojo.getId(), e);
            return;
        }
        // process
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
        SqlParams sqlParams = CommonDtoHelper.convert(connectionPojo, reportQueryPojo, inputParamMap, file, errorFile,
                properties.getMaxExecutionTime());
        try {
            String fQuery = SqlCmd.prepareQuery(inputParamMap, reportQueryPojo.getQuery(),
                    properties.getMaxExecutionTime());
            sqlParams.setQuery(fQuery);
            // Execute query and save results
            saveResultsOnCloud(pojo, sqlParams);
        } catch (Exception e) {
            log.error("Report ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED,
                    "Error while processing query : " + e.getMessage(), 0, 0.0);
        }
    }

    @Async("reportScheduleExecutor")
    public void runScheduleReportAsync(ReportRequestPojo pojo) throws ApiException, IOException {
        // mark as processing - locking
        try {
            api.markProcessingIfEligible(pojo.getId());
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException | ApiException e) {
            log.debug("Error occurred while marking report in progress for request id : " + pojo.getId(), e);
            return;
        }
        // process
        ReportRequestPojo reportRequestPojo = api.getCheck(pojo.getId());
        ReportPojo reportPojo = reportApi.getCheck(reportRequestPojo.getReportId());
        ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(reportRequestPojo.getOrgId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());

        // Creation of file
        File file = folderApi.getFileForExtension(reportRequestPojo.getId(), ".tsv");
        File errorFile = folderApi.getErrFile(reportRequestPojo.getId(), ".tsv");
        Map<String, String> inputParamMap = new HashMap<>();
        SqlParams sqlParams = CommonDtoHelper.convert(connectionPojo, reportQueryPojo, inputParamMap, file, errorFile,
                properties.getMaxExecutionTime());
        try {
            String fQuery = SqlCmd.prepareQuery(inputParamMap, reportQueryPojo.getQuery(),
                    properties.getMaxExecutionTime());
            sqlParams.setQuery(fQuery);
            // Execute query and send results
            saveResultsOnCloud(pojo, sqlParams);
        } catch (Exception e) {
            log.error("Report ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED,
                    "Error while processing query : " + e.getMessage(), 0, 0.0);
        }
    }

    private void saveResultsOnCloud(ReportRequestPojo pojo, SqlParams sqlParams) {
        try {
            // Process data
            SqlCmd.processQuery(sqlParams, false, properties.getMaxExecutionTime());
            String name = sqlParams.getOutFile().getName().split(".tsv")[0] + ".csv";
            File csvFile = folderApi.getFile(name);
            Integer noOfRows = FileUtil.getCsvFromTsv(sqlParams.getOutFile(), csvFile);

            // upload result to cloud
            String filePath = uploadFile(csvFile, pojo);
            double fileSize = FileUtil.getSizeInMb(csvFile.length());
            switch (pojo.getType()) {
                case EMAIL:
                    sendEmail(fileSize, csvFile);
                    break;
                case USER:
                    break;
            }
            // update status to completed
            api.updateStatus(pojo.getId(), ReportRequestStatus.COMPLETED, filePath, noOfRows, fileSize);
            FileUtil.delete(csvFile);
        } catch (Exception e) {
            // log as error and mark fail
            log.error("Report ID : " + pojo.getId() + " failed", e);
            try {
                String message = String.join("\t", Files.readAllLines(sqlParams.getErrFile().toPath()));
                api.markFailed(pojo.getId(), ReportRequestStatus.FAILED, message, 0, 0.0);
            } catch (Exception ex) {
                log.error("Error while updating the status of failed request", ex);
            }
        } finally {
            deleteFiles(sqlParams.getOutFile(), sqlParams.getErrFile());
        }
    }

    private void sendEmail(double fileSize, File csvFile) throws IOException, ApiException {
        if (fileSize > 20.0) {
            String outFileName = csvFile.getName().split(".csv")[0] + ".7z";
            File zipFile = folderApi.getFile(outFileName);
            try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(zipFile)) {
                SevenZArchiveEntry archiveEntry = sevenZOutput.createArchiveEntry(csvFile, csvFile.getName());
                sevenZOutput.putArchiveEntry(archiveEntry);
                sevenZOutput.write(Files.readAllBytes(csvFile.toPath()));
                sevenZOutput.closeArchiveEntry();
            } catch (IOException e) {
                log.error("Error while zipping : ", e);
            }
        }
        // todo send email
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
        } catch (FileClientException e) {
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
