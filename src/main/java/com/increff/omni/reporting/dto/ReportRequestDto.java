package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.InputControlFilterData;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.TimeZoneData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.fileclient.GcpFileProvider;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.convertToTimeZoneData;
import static com.increff.omni.reporting.dto.CommonDtoHelper.validate;

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
    private GcpFileProvider gcpFileProvider;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;

    private static final Integer MAX_NUMBER_OF_ROWS = 50;
    private static final Integer MAX_LIMIT = 50;
    private static final List<String> accessControlledKeys = Arrays.asList(ResourceQueryParamKeys.clientQueryParam,
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
        ReportQueryPojo reportQueryPojo = queryApi.getByReportId(reportPojo.getId());
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        if (Objects.isNull(reportQueryPojo))
            throw new ApiException(ApiStatus.BAD_DATA, "No query defined for report : " + reportPojo.getName());
        validateCustomReportAccess(reportPojo, orgId);
        validateInputParamValues(form.getParamMap(), inputParamsMap, orgId, inputDisplayMap, inputControlPojoList, ReportRequestType.USER);
        Map<String, String> inputDisplayStringMap = UserPrincipalUtil.getStringToStringParamMap(inputDisplayMap);
        List<ReportInputParamsPojo> reportInputParamsPojoList =
                CommonDtoHelper.getReportInputParamsPojoList(inputParamsMap, form.getTimezone(), orgId, inputDisplayStringMap);
        flow.requestReport(pojo, reportInputParamsPojoList);
        flow.saveAudit(pojo.getId().toString(), AuditActions.REQUEST_REPORT.toString(), "Request Report",
                "Report request submitted for organization : " + organizationPojo.getName(), getUserName());
    }

    public List<ReportRequestData> getAll() throws ApiException, IOException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList = reportRequestApi.getByUserId(getUserId(), MAX_LIMIT);
        for (ReportRequestPojo r : reportRequestPojoList) {
            ReportPojo reportPojo = reportApi.getCheck(r.getReportId());
            reportRequestDataList.add(getReportRequestData(r, reportPojo));
        }
        return reportRequestDataList;
    }

    public File getReportFile(Integer requestId) throws ApiException, IOException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        validate(requestPojo, requestId, reportPojo, getUserId(), getOrgId());
        String reportName = requestId + "_" + UUID.randomUUID();
        File sourceFile = folderApi.getFile(reportName + ".csv");
        byte[] data = getFileFromUrl(requestPojo.getUrl());
        FileUtils.writeByteArrayToFile(sourceFile, data);
        return sourceFile;
    }

    public List<Map<String, String>> getJsonFromCsv(Integer requestId) throws ApiException, IOException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        if (requestPojo.getStatus().equals(ReportRequestStatus.FAILED))
            throw new ApiException(ApiStatus.BAD_DATA, "Failed report can't be viewed");
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        validate(requestPojo, requestId, reportPojo, getUserId(), getOrgId());
        if (requestPojo.getNoOfRows() >= MAX_NUMBER_OF_ROWS)
            throw new ApiException(ApiStatus.BAD_DATA, "Data contains more than 50 rows. View option is restricted");
        List<Map<String, String>> data = new ArrayList<>();
        String reportName = requestId + "_" + UUID.randomUUID();
        File sourceFile = folderApi.getFile(reportName + ".csv");
        byte[] bytes = getFileFromUrl(requestPojo.getUrl());
        FileUtils.writeByteArrayToFile(sourceFile, bytes);
        Reader in = new FileReader(sourceFile);
        Iterable<CSVRecord> records =
                CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(in);
        for (CSVRecord record : records) {
            Map<String, String> value = record.toMap();
            data.add(value);
        }
        in.close();
        FileUtil.delete(sourceFile);
        return data;
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

    private ReportRequestData getReportRequestData(ReportRequestPojo pojo, ReportPojo reportPojo) throws ApiException {
        List<ReportInputParamsPojo> paramsPojoList = reportInputParamsApi.getInputParamsForReportRequest(pojo.getId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(pojo.getOrgId());
        ReportRequestData data = new ReportRequestData();
        data.setRequestCreationTime(pojo.getCreatedAt());
        data.setRequestUpdatedTime(pojo.getUpdatedAt());
        data.setStatus(pojo.getStatus());
        data.setType(pojo.getType());
        data.setRequestId(pojo.getId());
        data.setReportId(reportPojo.getId());
        data.setReportName(reportPojo.getName());
        data.setOrgName(organizationPojo.getName());
        data.setFileSize(pojo.getFileSize());
        data.setNoOfRows(pojo.getNoOfRows());
        data.setFailureReason(pojo.getFailureReason());
        setFiltersApplied(paramsPojoList, pojo, data);
        return data;
    }

    private byte[] getFileFromUrl(String url) throws IOException {
        return IOUtils.toByteArray(gcpFileProvider.get(url));
    }

    private void setFiltersApplied(List<ReportInputParamsPojo> paramsPojoList,
                                   ReportRequestPojo pojo,
                                   ReportRequestData data) {
        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(pojo.getReportId());
        List<Integer> controlIds = reportControlsPojos.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
        List<InputControlPojo> controlPojos = controlApi.selectByIds(controlIds);
        List<InputControlFilterData> filterData = new ArrayList<>();
        for (ReportInputParamsPojo reportInputParamsPojo : paramsPojoList) {
            if (accessControlledKeys.contains(reportInputParamsPojo.getParamKey())
                    || reportInputParamsPojo.getParamKey().equals("orgId"))
                continue;
            if (reportInputParamsPojo.getParamKey().equals("timezone")) {
                data.setTimezone(getValueFromQuotes(reportInputParamsPojo.getParamValue()));
                continue;
            }
            Optional<InputControlPojo> controlPojo =
                    controlPojos.stream().filter(c -> c.getParamName().equals(reportInputParamsPojo.getParamKey()))
                            .findFirst();
            if (controlPojo.isPresent()) {
                InputControlFilterData d = new InputControlFilterData();
                d.setType(controlPojo.get().getType());
                d.setParamName(controlPojo.get().getParamName());
                d.setDisplayName(controlPojo.get().getDisplayName());
                List<String> values = Objects.isNull(reportInputParamsPojo.getDisplayValue()) ? new ArrayList<>() :
                        Arrays.stream(reportInputParamsPojo.getDisplayValue().split(
                                        ","))
                                .map(this::getValueFromQuotes).collect(Collectors.toList());
                d.setValues(values);
                filterData.add(d);
            }
        }
        data.setFilters(filterData);
    }
}
