package com.increff.omni.reporting.dto;

import com.increff.commons.queryexecutor.data.QueryRequestData;
import com.increff.commons.queryexecutor.form.GetRequestForm;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ResourceQueryParamKeys;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.TimeZoneData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUploadUtil;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.*;

@Service
@Log4j
public class ReportRequestDto extends AbstractDto {

    @Autowired
    private ReportRequestFlowApi flow;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private ReportRequestApi reportRequestApi;
    @Autowired
    private CustomReportAccessApi customReportAccessApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ReportQueryApi queryApi;
    @Autowired
    private InputControlFlowApi inputControlFlowApi;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private FileUploadUtil fileUploadUtil;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;
    @Autowired
    private QueryExecutorClientApi executorClientApi;

    private static final Integer MAX_NUMBER_OF_ROWS = 50;
    private static final Integer MAX_LIMIT = 25;
    public static final List<String> accessControlledKeys = Arrays.asList(ResourceQueryParamKeys.clientQueryParam,
            ResourceQueryParamKeys.fulfillmentLocationQueryParamKey);

    public void requestReport(ReportRequestForm form) throws ApiException {
        requestReportForAnyOrg(form, getOrgId());
    }

    public void requestReportForAnyOrg(ReportRequestForm form, Integer orgId) throws ApiException {
        checkValid(form);
        OrganizationPojo organizationPojo = organizationApi.getCheck(orgId);
        Map<String, String> inputParamsMap = UserPrincipalUtil.getCompleteMapWithAccessControl(form.getParamMap());
        Map<String, List<String>> inputDisplayMap = new HashMap<>();
        ReportRequestPojo pojo = CommonDtoHelper.getReportRequestPojo(form, orgId, getUserId());
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        if (reportPojo.getIsDashboard())
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard can't be requested here");
        ReportQueryPojo reportQueryPojo = queryApi.getByReportId(reportPojo.getId());
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        if (Objects.isNull(reportQueryPojo))
            throw new ApiException(ApiStatus.BAD_DATA, "No query defined for report : " + reportPojo.getName());
        validateCustomReportAccess(reportPojo, orgId);
        validateInputParamValues(form.getParamMap(), inputParamsMap, orgId, inputDisplayMap, inputControlPojoList,
                ReportRequestType.USER);
        Map<String, String> inputDisplayStringMap = UserPrincipalUtil.getStringToStringParamMap(inputDisplayMap);
        List<ReportInputParamsPojo> reportInputParamsPojoList =
                CommonDtoHelper.getReportInputParamsPojoList(inputParamsMap, form.getTimezone(), orgId,
                        inputDisplayStringMap);
        flow.requestReport(pojo, reportInputParamsPojoList);
        flow.saveAudit(pojo.getId().toString(), AuditActions.REQUEST_REPORT.toString(), "Request Report",
                "Report request submitted for organization : " + organizationPojo.getName(), getUserName());
    }

    public List<ReportRequestData> getAll() throws ApiException, IOException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList = reportRequestApi.getByUserId(getUserId(), MAX_LIMIT);
        List<Integer> pendingRequestIds = reportRequestPojoList.stream().filter(r -> r.getStatus().equals(ReportRequestStatus.REQUESTED))
                        .map(ReportRequestPojo::getId).collect(
                        Collectors.toList());
        updatePendingRequestStatus(pendingRequestIds, reportRequestPojoList, getUserId());
        List<Integer> reportRequestIds =
                reportRequestPojoList.stream().map(ReportRequestPojo::getId).collect(Collectors.toList());
        reportRequestPojoList = reportRequestApi.getByIds(reportRequestIds);
        List<Integer> reportIds =
                reportRequestPojoList.stream().map(ReportRequestPojo::getReportId).collect(Collectors.toList());
        List<Integer> orgIds =
                reportRequestPojoList.stream().map(ReportRequestPojo::getOrgId).collect(Collectors.toList());
        List<ReportInputParamsPojo> allParamsPojo =
                reportInputParamsApi.getInputParamsForReportRequestIds(reportRequestIds);
        Map<Integer, List<ReportInputParamsPojo>> requestToParamsMap = prepareRequestToParamMap(allParamsPojo);
        List<ReportPojo> reportPojoList = reportApi.getByIds(reportIds, false);
        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportIds(reportIds);
        List<Integer> controlIds = reportControlsPojos.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
        List<InputControlPojo> controlPojos = controlApi.selectByIds(controlIds);
        List<OrganizationPojo> organizationPojoList = organizationApi.getCheck(orgIds);
        Map<Integer, OrganizationPojo> orgToPojo = prepareOrgIdToPojo(organizationPojoList);

        for (ReportRequestPojo r : reportRequestPojoList) {
            Optional<ReportPojo> reportPojo =
                    reportPojoList.stream().filter(report -> report.getId().equals(r.getReportId())).findFirst();
            if(!reportPojo.isPresent())
                continue;
            List<ReportInputParamsPojo> paramsPojoList = requestToParamsMap.get(r.getId());
            OrganizationPojo organizationPojo = orgToPojo.get(r.getOrgId());
            reportRequestDataList.add(
                    getReportRequestData(r, reportPojo.get(), controlPojos, paramsPojoList, organizationPojo));
        }
        return reportRequestDataList;
    }

    public String getReportFile(Integer requestId) throws ApiException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        if (!requestPojo.getType().equals(ReportRequestType.USER))
            throw new ApiException(ApiStatus.BAD_DATA, "Scheduled reports can't be downloaded");
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        validate(requestPojo, requestId, reportPojo, getUserId());
        return fileUploadUtil.getSignedUri(requestPojo.getUrl()).toString();
    }

    public List<Map<String, String>> viewReport(Integer requestId) throws ApiException, IOException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        if (!requestPojo.getType().equals(ReportRequestType.USER))
            throw new ApiException(ApiStatus.BAD_DATA, "Scheduled reports can't be viewed");
        if (requestPojo.getStatus().equals(ReportRequestStatus.FAILED))
            throw new ApiException(ApiStatus.BAD_DATA, "Failed report can't be viewed");
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        validate(requestPojo, requestId, reportPojo, getUserId());
        if (requestPojo.getNoOfRows() >= MAX_NUMBER_OF_ROWS)
            throw new ApiException(ApiStatus.BAD_DATA, "Data contains more than 50 rows. View option is restricted");
        String reportName = requestId + "_" + UUID.randomUUID();
        File sourceFile = folderApi.getFile(reportName + ".csv");
        byte[] bytes = getFileFromUrl(requestPojo.getUrl());
        FileUtils.writeByteArrayToFile(sourceFile, bytes);
        List<Map<String, String>> data = FileUtil.getJsonDataFromFile(sourceFile, ',');
        FileUtil.delete(sourceFile);
        return data;

    }

    public void updatePendingRequestStatus(List<Integer> pendingRequestIds, List<ReportRequestPojo> reportRequestPojoList,
                                      int userId) throws ApiException {
        if(pendingRequestIds.isEmpty())
            return;
        GetRequestForm form = new GetRequestForm();
        List<Long> requestIds = pendingRequestIds.stream().map(Long::valueOf).collect(Collectors.toList());
        form.setReferenceIds(requestIds);
        form.setUserId(userId);
        List<QueryRequestData> data = executorClientApi.getQueryRequestDataList(form);
        for(QueryRequestData d : data) {
            Optional<ReportRequestPojo> requestPojo =
                    reportRequestPojoList.stream().filter(r -> r.getId().equals(d.getReferenceId().intValue())).findFirst();
            if(requestPojo.isPresent()) {
                // This happens separately in a separate transaction
                reportRequestApi.updateStatus(requestPojo.get().getId(), getStatusMapping(d.getStatus()),
                        requestPojo.get().getUrl(), d.getNoOfRows(), d.getFileSize(), d.getFailureReason());
            }
        }
    }

    public List<TimeZoneData> getAllAvailableTimeZones() throws ApiException {
        Set<String> timeZoneIds = ZoneId.getAvailableZoneIds();
        List<TimeZoneData> dataList = new ArrayList<>();
        for (String timeZoneId : timeZoneIds) {
            dataList.add(convertToTimeZoneData(timeZoneId));
        }
        dataList.sort(Comparator.comparing(TimeZoneData::getZoneId));
        return dataList;
    }

    private byte[] getFileFromUrl(String url) throws IOException {
        return IOUtils.toByteArray(fileUploadUtil.get(url));
    }
}
