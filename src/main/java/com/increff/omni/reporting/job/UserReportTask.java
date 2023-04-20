package com.increff.omni.reporting.job;

import com.increff.commons.queryexecutor.QueryExecutorClient;
import com.increff.commons.queryexecutor.form.FileUploadDetailsForm;
import com.increff.commons.queryexecutor.form.QueryDetailsForm;
import com.increff.commons.queryexecutor.form.QueryExecutorForm;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getInputParamMapFromPojoList;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getValueFromQuotes;

@Component
@Log4j
public class UserReportTask extends AbstractTask{

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
    private ApplicationProperties properties;
    @Autowired
    private QueryExecutorClient executorClient;

    @Override
    @Async("userReportRequestExecutor")
    protected void runReportAsync(ReportRequestPojo pojo) throws ApiException {
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
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            String timezone = getValueFromQuotes(inputParamMap.get("timezone"));
            String fQuery = SqlCmd.getFinalQuery(inputParamMap, reportQueryPojo.getQuery(),
                    false);
            QueryExecutorForm queryExecutorForm = getQueryExecutorForm(fQuery, timezone, connectionPojo,
                    reportRequestPojo, reportPojo);
            executorClient.postRequest(queryExecutorForm);
            api.updateStatus(pojo.getId(), ReportRequestStatus.REQUESTED, "", 0, 0.0, "");
        } catch (Exception e) {
            log.error("Report Request ID : " + pojo.getId() + " failed", e);
            api.markFailed(pojo.getId(), ReportRequestStatus.FAILED, e.getMessage(), 0, 0.0);
        }
    }

    private QueryExecutorForm getQueryExecutorForm(String fQuery, String timezone, ConnectionPojo connectionPojo,
                                                   ReportRequestPojo reportRequestPojo,
                                                   ReportPojo reportPojo) {
        QueryExecutorForm form = new QueryExecutorForm();
        form.setUserId(reportRequestPojo.getUserId());
        form.setReferenceId(Long.valueOf(reportRequestPojo.getId()));
        FileUploadDetailsForm uploadDetailsForm = new FileUploadDetailsForm();
        uploadDetailsForm.setFileFormat(reportRequestPojo.getFileFormat());
        uploadDetailsForm.setGcpBucketName(properties.getGcpBucketName());
        uploadDetailsForm.setFilename(reportPojo.getName());
        uploadDetailsForm.setMaxFileSize(properties.getMaxFileSize());
        uploadDetailsForm.setFilepath(getFilePath(reportRequestPojo));
        uploadDetailsForm.setTimezone(timezone);
        QueryDetailsForm queryDetailsForm = new QueryDetailsForm();
        queryDetailsForm.setQuery(fQuery);
        queryDetailsForm.setConnectTimeout(properties.getMaxConnectionTime());
        queryDetailsForm.setPassword(connectionPojo.getPassword());
        queryDetailsForm.setUsername(connectionPojo.getUsername());
        queryDetailsForm.setReadTimeout(properties.getMaxExecutionTime());
        queryDetailsForm.setHost(connectionPojo.getHost());
        form.setFileUploadDetails(uploadDetailsForm);
        form.setQueryDetails(queryDetailsForm);
        return form;
    }

    private String getFilePath(ReportRequestPojo pojo) {
        return pojo.getOrgId() + "/" + "REPORTS" + "/" + pojo.getId() + "_" + UUID.randomUUID() + "." +
                pojo.getFileFormat().toString().toLowerCase();
    }
}
