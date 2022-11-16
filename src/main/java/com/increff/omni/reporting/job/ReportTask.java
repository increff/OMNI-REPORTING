package com.increff.omni.reporting.job;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;

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

    @Async("jobExecutor")
    public void runAsync(ReportRequestPojo pojo) throws ApiException, IOException {
        // mark as processing - locking
        api.markProcessingIfEligible(pojo.getId());

        // process
        ReportRequestPojo reportRequestPojo = api.getCheck(pojo.getId());
        List<ReportInputParamsPojo> reportInputParamsPojoList = reportInputParamsApi.getInputParamsForReportRequest(reportRequestPojo.getId());
        ReportPojo reportPojo = reportApi.getCheck(reportRequestPojo.getReportId());
        ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
        if(Objects.isNull(reportQueryPojo))
            throw new ApiException(ApiStatus.BAD_DATA, "Query is not defined for requested report : " + reportPojo.getName());
        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(reportRequestPojo.getOrgId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());

        // Creation of file
        File file = folderApi.getFileForExtension(reportRequestPojo.getId(), ".tsv");
        File errorFile = folderApi.getErrFile(reportRequestPojo.getId(), ".tsv");
        Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
        SqlParams sqlParams = CommonDtoHelper.convert(connectionPojo, reportQueryPojo, inputParamMap, file, errorFile, properties.getMaxExecutionTime());
        // Execute query and save results
        saveResultsOnCloud(pojo, sqlParams);
    }

    private void saveResultsOnCloud(ReportRequestPojo pojo, SqlParams sqlParams) {
        try {
            // Process data
            SqlCmd.processQuery(sqlParams, false);
            String name = sqlParams.getOutFile().getName().split(".tsv")[0] + ".csv";
            File csvFile = folderApi.getFile(name);
            Integer noOfRows = FileUtil.getCsvFromTsv(sqlParams.getOutFile(), csvFile);

            // upload result to cloud
            String filePath = uploadFile(csvFile, "SUCCESS_REPORTS", pojo);
            // update status to completed
            Double fileSize = FileUtil.getSizeInMb(csvFile.length());
            api.updateStatus(pojo.getId(), ReportRequestStatus.COMPLETED, filePath, noOfRows, fileSize);
            FileUtil.delete(csvFile);
        } catch (Exception e) {
            // log as error and mark fail
            log.error("Report ID : " + pojo.getId() + " failed", e);
            try {
                String name = sqlParams.getErrFile().getName().split(".tsv")[0] + ".csv";
                File csvFile = folderApi.getFile(name);
                Integer noOfRows = FileUtil.getCsvFromTsv(sqlParams.getErrFile(), csvFile);
                String filePath = uploadFile(csvFile, "ERROR_REPORTS", pojo);
                Double fileSize = FileUtil.getSizeInMb(csvFile.length());
                api.updateStatus(pojo.getId(), ReportRequestStatus.FAILED, filePath, noOfRows, fileSize);
                FileUtil.delete(csvFile);
            } catch (Exception ex) {
                log.error("Error while updating the status of failed request", ex);
            }
        } finally {
            deleteFiles(sqlParams.getOutFile(), sqlParams.getErrFile());
        }
    }

    private void deleteFiles(File outFile, File errFile) {
        if (outFile.exists() && !FileUtil.delete(outFile))
            log.debug("File deletion failed, name : " + outFile.getName());
        if (errFile.exists() && !FileUtil.delete(errFile))
            log.debug("File deletion failed, name : " + errFile.getName());
    }

    private String uploadFile(File file, String folder, ReportRequestPojo pojo) throws FileNotFoundException, ApiException {
        InputStream inputStream = new FileInputStream(file);
        String filePath = pojo.getOrgId() + "/" + folder + "/" + pojo.getId() + "_" + UUID.randomUUID() + ".csv";
        try {
            fileClient.create(filePath, inputStream);
        } catch (FileClientException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error in uploading Report File to Gcp for report : " +
                    pojo.getId());
        }
        return properties.getGcpBaseUrl() + "/" + properties.getGcpBucketName() + "/" + filePath;
    }

    private Map<String, String> getInputParamMapFromPojoList(List<ReportInputParamsPojo> reportInputParamsPojoList) {
        Map<String, String> inputParamMap = new HashMap<>();
        reportInputParamsPojoList.forEach(r -> inputParamMap.put(r.getParamKey(), r.getParamValue()));
        return inputParamMap;
    }

}
