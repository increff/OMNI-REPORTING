package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.client.ReportingClient;
import com.increff.omni.reporting.flow.ReportRequestFlow;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j
public class ReportRequestDto extends AbstractDto {

    @Autowired
    private ReportRequestFlow flow;
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
    private ReportingClient client;

    public void requestReport(ReportRequestForm form) throws ApiException {
        checkValid(form);
        ReportRequestPojo pojo = CommonDtoHelper.getReportRequestPojo(form, getOrgId(), getUserId());
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        validateCustomReportAccess(reportPojo, getOrgId());
        validateInputParamValues(reportPojo, form.getParamMap());
        List<ReportInputParamsPojo> reportInputParamsPojoList = CommonDtoHelper.getReportInputParamsPojoList(form.getParamMap());
        flow.requestReport(pojo, form.getParamMap(), reportInputParamsPojoList);
    }

    public List<ReportRequestData> getAll(Integer days) throws ApiException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList = reportRequestApi.getByUserId(getUserId(), days);
        for (ReportRequestPojo r : reportRequestPojoList) {
            ReportPojo reportPojo = reportApi.getCheck(r.getReportId());
            reportRequestDataList.add(CommonDtoHelper.getReportRequestData(r, reportPojo));
        }
        return reportRequestDataList;
    }

    public File getReportFile(Integer requestId) throws ApiException, IOException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        if (requestPojo.getUserId() != getUserId()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Logged in user has not requested the report with id : " + requestId);
        }
        if (!Arrays.asList(ReportRequestStatus.COMPLETED, ReportRequestStatus.FAILED).contains(requestPojo.getStatus())) {
            throw new ApiException(ApiStatus.BAD_DATA, "Report request is still in processing, name : " + reportPojo.getName());
        }
        String reportName = reportPojo.getName() + "-" +
                requestPojo.getUpdatedAt().toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        File sourceFile = folderApi.getFile(reportName + ".xls");
        byte[] data = client.getFileFromUrl(requestPojo.getUrl());
        FileUtils.writeByteArrayToFile(sourceFile, data);
        return sourceFile;
    }

    private void validateCustomReportAccess(ReportPojo reportPojo, Integer orgId) throws ApiException {
        if (reportPojo.getType().equals(ReportType.STANDARD))
            return;
        CustomReportAccessPojo customReportAccessPojo = customReportAccessApi.getByReportAndOrg(reportPojo.getId(), orgId);
        if (Objects.isNull(customReportAccessPojo)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Organization does not have access to view this report : " + reportPojo.getName());
        }
    }

    private void validateInputParamValues(ReportPojo reportPojo, Map<String, String> params) throws ApiException {
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectMultiple(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        for (InputControlPojo i : inputControlPojoList) {
            if (params.containsKey(i.getParamName())) {
                String value = params.get(i.getParamName());
                if (StringUtil.isEmpty(value))
                    continue;
                String[] values;
                Map<String, String> allowedValuesMap;
                switch (i.getType()) {
                    case TEXT:
                        break;
                    case NUMBER:
                        try {
                            Integer.parseInt(value);
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA, value + " is not a number for filter : " + i.getDisplayName());
                        }
                        break;
                    case DATE:
                        try {
                            ZonedDateTime.parse(value);
                        } catch (Exception e) {
                            throw new ApiException(ApiStatus.BAD_DATA, value + " is not in valid date format for filter : " + i.getDisplayName());
                        }
                        break;
                    case SINGLE_SELECT:
                        values = value.split(",");
                        allowedValuesMap = checkValidValues(i);
                        if (values.length > 1)
                            throw new ApiException(ApiStatus.BAD_DATA, "Multiple values not allowed for filter : " + i.getDisplayName());
                        String s = values[0].substring(1, values[0].length() - 1);
                        if (!allowedValuesMap.containsKey(s))
                            throw new ApiException(ApiStatus.BAD_DATA, values[0] + " is not allowed for filter : " + i.getDisplayName());
                        break;
                    case MULTI_SELECT:
                        values = value.split(",");
                        allowedValuesMap = checkValidValues(i);
                        for (String v : values) {
                            v = v.substring(1, v.length() - 1);
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : " + i.getDisplayName());

                        }
                        break;
                    default:
                        throw new ApiException(ApiStatus.BAD_DATA, "Invalid Input Control Type");
                }
            } else {
                throw new ApiException(ApiStatus.BAD_DATA, "Param key not present in request : " + i.getDisplayName());
            }
        }
    }

    private Map<String, String> getValuesFromQuery(String query) throws ApiException {
        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(getOrgId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
        return getInputParamValueMap(connectionPojo, query);
    }

    private Map<String, String> checkValidValues(InputControlPojo p) throws ApiException {
        Map<String, String> valuesMap = new HashMap<>();
        List<InputControlQueryPojo> queryPojoList = controlApi.selectControlQueries(Collections.singletonList(p.getId()));
        if (queryPojoList.isEmpty()) {
            List<InputControlValuesPojo> valuesPojoList = controlApi.selectControlValues(Collections.singletonList(p.getId()));
            for (InputControlValuesPojo pojo : valuesPojoList) {
                valuesMap.put(pojo.getValue(), pojo.getValue());
            }
        } else {
            valuesMap = getValuesFromQuery(queryPojoList.get(0).getQuery());
        }
        return valuesMap;
    }
}
