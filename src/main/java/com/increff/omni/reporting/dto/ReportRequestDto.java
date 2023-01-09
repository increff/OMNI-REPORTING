package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ResourceQueryParamKeys;
import com.increff.omni.reporting.model.data.InputControlFilterData;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.TimeZoneData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.fileclient.GcpFileProvider;
import com.nextscm.commons.lang.StringUtil;
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
import java.time.ZonedDateTime;
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
    private static final Integer MAX_LIST_SIZE = 1000;
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
        if (Objects.isNull(reportQueryPojo))
            throw new ApiException(ApiStatus.BAD_DATA, "No query defined for report : " + reportPojo.getName());
        validateCustomReportAccess(reportPojo, orgId);
        validateInputParamValues(reportPojo, form.getParamMap(), inputParamsMap, orgId, inputDisplayMap);
        Map<String, String> inputDisplayStringMap = UserPrincipalUtil.getStringToStringParamMap(inputDisplayMap);
        List<ReportInputParamsPojo> reportInputParamsPojoList =
                CommonDtoHelper.getReportInputParamsPojoList(inputParamsMap, form.getTimezone(), orgId, inputDisplayStringMap);
        flow.requestReport(pojo, reportInputParamsPojoList);
        flow.saveAudit(reportPojo.getId().toString(), AuditActions.REQUEST_REPORT.toString(), "Request Report",
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
        validate(requestPojo, requestId, reportPojo, getUserId());
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
        validate(requestPojo, requestId, reportPojo, getUserId());
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

    private void validateCustomReportAccess(ReportPojo reportPojo, Integer orgId) throws ApiException {
        if (reportPojo.getType().equals(ReportType.STANDARD))
            return;
        CustomReportAccessPojo customReportAccessPojo =
                customReportAccessApi.getByReportAndOrg(reportPojo.getId(), orgId);
        if (Objects.isNull(customReportAccessPojo)) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization does not have access to view this report : " + reportPojo.getName());
        }
    }

    private ReportRequestData getReportRequestData(ReportRequestPojo pojo, ReportPojo reportPojo) throws ApiException {
        List<ReportInputParamsPojo> paramsPojoList = reportInputParamsApi.getInputParamsForReportRequest(pojo.getId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(pojo.getOrgId());
        ReportRequestData data = new ReportRequestData();
        data.setRequestCreationTime(pojo.getCreatedAt());
        data.setRequestUpdatedTime(pojo.getUpdatedAt());
        data.setStatus(pojo.getStatus());
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

    private void validateInputParamValues(ReportPojo reportPojo, Map<String, List<String>> inputParams,
                                          Map<String, String> params, int orgId,
                                          Map<String, List<String>> inputDisplayMap) throws ApiException {
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));

        for (InputControlPojo i : inputControlPojoList) {
            if (params.containsKey(i.getParamName())) {
                String value = params.get(i.getParamName());
                if (StringUtil.isEmpty(value) || value.equals("''")) {
                    params.put(i.getParamName(), null);
                    continue;
                }
                List<String> values;
                Map<String, String> allowedValuesMap;
                List<String> displayNames = new ArrayList<>();
                switch (i.getType()) {
                    case TEXT:
                    case MULTI_TEXT:
                        break;
                    case NUMBER:
                        try {
                            value = getValueFromQuotes(value);
                            Integer.parseInt(value);
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA, value + " is not a number for filter : "
                                    + i.getDisplayName());
                        }
                        break;
                    case DATE:
                    case DATE_TIME:
                        try {
                            value = getValueFromQuotes(value);
                            ZonedDateTime.parse(value);
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA,
                                    value + " is not in valid date format for filter : " + i.getDisplayName());
                        }
                        break;
                    case SINGLE_SELECT:
                        values = inputParams.get(i.getParamName());
                        allowedValuesMap = checkValidValues(i, orgId);
                        if (values.size() > 1)
                            throw new ApiException(ApiStatus.BAD_DATA, "Multiple values not allowed for filter : "
                                    + i.getDisplayName());
                        String s = values.get(0);
                        if (!allowedValuesMap.containsKey(s))
                            throw new ApiException(ApiStatus.BAD_DATA, values.get(0) + " is not allowed for filter : "
                                    + i.getDisplayName());
                        displayNames.add(allowedValuesMap.get(s));
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    case ACCESS_CONTROLLED_MULTI_SELECT:
                        values = inputParams.get(i.getParamName());
                        allowedValuesMap = checkValidValues(i, orgId);
                        for (String v : values) {
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : "
                                        + i.getDisplayName());
                            displayNames.add(allowedValuesMap.get(v));
                        }
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    case MULTI_SELECT:
                        values = inputParams.get(i.getParamName());
                        if (values.size() > MAX_LIST_SIZE)
                            throw new ApiException(ApiStatus.BAD_DATA,
                                    i.getDisplayName() + " can't have more than " + MAX_LIST_SIZE + " values in " +
                                            "single request");
                        allowedValuesMap = checkValidValues(i, orgId);
                        for (String v : values) {
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : "
                                        + i.getDisplayName());
                            displayNames.add(allowedValuesMap.get(v));
                        }
                        inputDisplayMap.put(i.getParamName(), displayNames);
                        break;
                    default:
                        throw new ApiException(ApiStatus.BAD_DATA, "Invalid Input Control Type");
                }
            } else {
                params.put(i.getParamName(), null);
            }
        }
    }

    private byte[] getFileFromUrl(String url) throws IOException {
        return IOUtils.toByteArray(gcpFileProvider.get(url));
    }

    private Map<String, String> checkValidValues(InputControlPojo p, int orgId) throws ApiException {
        Map<String, String> valuesMap = new HashMap<>();
        InputControlQueryPojo queryPojo = controlApi.selectControlQuery(p.getId());
        if (Objects.isNull(queryPojo)) {
            List<InputControlValuesPojo> valuesPojoList =
                    controlApi.selectControlValues(Collections.singletonList(p.getId()));
            for (InputControlValuesPojo pojo : valuesPojoList) {
                valuesMap.put(pojo.getValue(), pojo.getValue());
            }
        } else {
            OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(orgId);
            ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
            valuesMap = inputControlFlowApi.getValuesFromQuery(queryPojo.getQuery(), connectionPojo);
        }
        return valuesMap;
    }

    private String getValueFromQuotes(String value) {
        try {
            return value.substring(1, value.length() - 1);
        } catch (Exception e) {
            return "";
        }
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
