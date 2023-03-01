package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportRequestFlowApi extends AbstractFlowApi {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;

    public void requestReport(ReportRequestPojo pojo, List<ReportInputParamsPojo> reportInputParamsPojoList)
            throws ApiException {
        validate(pojo, reportInputParamsPojoList);
        requestReportWithoutValidation(pojo, reportInputParamsPojoList);
    }

    public void requestReportWithoutValidation(ReportRequestPojo pojo,
                                               List<ReportInputParamsPojo> reportInputParamsPojoList) {
        api.add(pojo);
        reportInputParamsPojoList.forEach(r -> r.setReportRequestId(pojo.getId()));
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

    private void validate(ReportRequestPojo pojo, List<ReportInputParamsPojo> reportInputParamsPojoList)
            throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if (!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() >= MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");
        List<ReportValidationGroupPojo> reportValidationGroupPojoList = reportValidationGroupApi
                .getByReportId(reportPojo.getId());
        Map<String, List<ReportValidationGroupPojo>> groupedByName = reportValidationGroupPojoList.stream()
                .collect(Collectors.groupingBy(ReportValidationGroupPojo::getGroupName));

        // Run through all the validators for this report
        for (Map.Entry<String, List<ReportValidationGroupPojo>> validationList : groupedByName.entrySet()) {
            List<ReportValidationGroupPojo> groupPojoList = validationList.getValue();
            ValidationType type = groupPojoList.get(0).getType();
            List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByIds(groupPojoList
                    .stream().map(ReportValidationGroupPojo::getReportControlId).collect(Collectors.toList()));
            List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
            List<String> paramValues = new ArrayList<>();
            List<String> displayValues = new ArrayList<>();

            inputControlPojoList.forEach(i -> {
                ReportInputParamsPojo p = reportInputParamsPojoList.stream().filter(r -> r.getParamKey()
                        .equals(i.getParamName())).collect(Collectors.toList()).get(0);
                paramValues.add(p.getParamValue());
                displayValues.add(i.getDisplayName());
            });
            runValidators(reportPojo, groupPojoList, type, paramValues, displayValues, ReportRequestType.USER);
        }

    }

}
