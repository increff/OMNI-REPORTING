package com.increff.omni.reporting.job;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.SqlParams;
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
    public void runAsync(ReportRequestPojo pojo) {
        Integer id = pojo.getId();
        try {
            boolean isRunRequired = checkLock(id);
            if (!isRunRequired)
                return;
            // mark as processing
            api.markProcessingIfEligible(id);
            //process
            SqlParams sqlParams = runReportRequest(id);
            File outFile = sqlParams.getOutFile();
            File errFile = sqlParams.getErrFile();
            // upload result to cloud
            if (outFile.length() > 0) {
                String filePath = uploadFile(outFile, "SUCCESS_REPORTS", pojo);
                // update status to completed
                api.updateStatus(id, ReportRequestStatus.COMPLETED, filePath);
            } else if (errFile.length() > 0) {
                String filePath = uploadFile(errFile, "ERROR_REPORTS", pojo);
                // update status to failed as error file is having some content
                api.updateStatus(id, ReportRequestStatus.FAILED, filePath);
            }
            deleteFiles(outFile, errFile);
        } catch (Exception e) {
            // log as error and mark fail
            log.error("Report ID : " + id + " failed", e);
            try {
                api.updateStatus(id, ReportRequestStatus.FAILED, "");
            } catch (ApiException ex) {
                log.error("Error while updating the status of request", ex);
            }
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
        String filePath = pojo.getOrgId() + "/" + folder + "/" + pojo.getId() + "_" + UUID.randomUUID() + ".xls";
        try {
            fileClient.create(filePath, inputStream);
        } catch (FileClientException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error in uploading Report File to Gcp for report : " +
                    pojo.getId());
        }
        return properties.getGcpBaseUrl() + "/" + properties.getGcpBucketName() + "/" + filePath;
    }

    private SqlParams runReportRequest(Integer id) throws ApiException, IOException {
        ReportRequestPojo reportRequestPojo = api.getCheck(id);
        List<ReportInputParamsPojo> reportInputParamsPojoList = reportInputParamsApi.getInputParamsForReportRequest(reportRequestPojo.getReportId());
        ReportPojo reportPojo = reportApi.getCheck(reportRequestPojo.getReportId());
        ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(reportRequestPojo.getOrgId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
        File file = folderApi.getFileForExtension(reportRequestPojo.getId(), ".xls");
        File errorFile = folderApi.getErrFile(reportRequestPojo.getId(), ".txt");
        Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
        SqlParams sqlParams = ReportTaskHelper.convert(connectionPojo, reportQueryPojo, inputParamMap, file, errorFile);
        SqlCmd.processQuery(sqlParams);
        return sqlParams;
    }

    private Map<String, String> getInputParamMapFromPojoList(List<ReportInputParamsPojo> reportInputParamsPojoList) {
        Map<String, String> inputParamMap = new HashMap<>();
        reportInputParamsPojoList.forEach(r -> inputParamMap.put(r.getParamKey(), r.getParamValue()));
        return inputParamMap;
    }

    private boolean checkLock(Integer id) throws ApiException {
        ReportRequestPojo reportRequestPojo = api.getCheck(id);
        return Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.STUCK).contains(reportRequestPojo.getStatus());
    }

}
