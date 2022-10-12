package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.client.ReportingClient;
import com.increff.omni.reporting.flow.ReportRequestFlow;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.helper.ReportRequestDtoHelper.getReportRequestData;
import static com.increff.omni.reporting.helper.ReportRequestDtoHelper.getReportRequestPojo;

@Service
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
    private FolderApi folderApi;
    @Autowired
    private ReportingClient client;

    public void requestReport(ReportRequestForm form) throws ApiException {
        checkValid(form);
        ReportRequestPojo pojo = getReportRequestPojo(form, getOrgId(), getUserId());
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        validateInputParamValues(reportPojo, form.getParamMap());
        List<ReportInputParamsPojo> reportInputParamsPojoList = getReportInputParamsPojoList(form.getParamMap());
        flow.requestReport(pojo, form.getParamMap(), reportInputParamsPojoList, getOrgId());
    }

    public List<ReportRequestData> getAll(Integer days) throws ApiException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList = reportRequestApi.getByUserId(getUserId(), days);
        for (ReportRequestPojo r : reportRequestPojoList) {
            ReportPojo reportPojo = reportApi.getCheck(r.getReportId());
            reportRequestDataList.add(getReportRequestData(r, reportPojo));
        }
        return reportRequestDataList;
    }

    public File getReportFile(Integer requestId) throws ApiException, IOException {
        ReportRequestPojo requestPojo = reportRequestApi.getCheck(requestId);
        if(requestPojo.getUserId() != getUserId()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Logged in user has not requested the report with id : " + requestId);
        }
        ReportPojo reportPojo = reportApi.getCheck(requestPojo.getReportId());
        File file = folderApi.getFile(reportPojo.getName() + " " + requestPojo.getId());
        byte[] data = client.getFileFromUrl(requestPojo.getUrl());
        FileUtils.writeByteArrayToFile(file, data);
        return file;
    }

    private void validateInputParamValues(ReportPojo reportPojo, Map<String, String> params) throws ApiException {
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectMultiple(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        for (InputControlPojo i : inputControlPojoList) {
            if (params.containsKey(i.getParamName())) {
                String value = params.get(i.getParamName());
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
                        if (!allowedValuesMap.containsKey(values[0]))
                            throw new ApiException(ApiStatus.BAD_DATA, values[0] + " is not allowed for filter : " + i.getDisplayName());
                        break;
                    case MULTI_SELECT:
                        values = value.split(",");
                        allowedValuesMap = checkValidValues(i);
                        for (String v : values)
                            if (!allowedValuesMap.containsKey(v))
                                throw new ApiException(ApiStatus.BAD_DATA, v + " is not allowed for filter : " + i.getDisplayName());
                        break;
                    default:
                        throw new ApiException(ApiStatus.BAD_DATA, "Invalid Input Control Type");
                }
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

    private List<ReportInputParamsPojo> getReportInputParamsPojoList(Map<String, String> paramMap) {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
        return reportInputParamsPojoList;
    }
}
