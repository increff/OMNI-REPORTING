package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
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
public class ReportRequestFlow extends AbstractApi {

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
    private MandatoryValidator mandatoryValidator;
    @Autowired
    private SingleMandatoryValidator singleMandatoryValidator;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private DateValidator dateValidator;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;

    public void requestReport(ReportRequestPojo pojo, Map<String, String> params, List<ReportInputParamsPojo> reportInputParamsPojoList) throws ApiException {
        validate(pojo, params);
        api.add(pojo);
        reportInputParamsPojoList.forEach(r -> r.setReportRequestId(pojo.getId()));
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

    private void validate(ReportRequestPojo pojo, Map<String, String> params) throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if (!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() > MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");
        List<ReportValidationGroupPojo> reportValidationGroupPojoList = reportValidationGroupApi.getByReportId(reportPojo.getId());
        Map<String, List<ReportValidationGroupPojo>> groupedByName = reportValidationGroupPojoList.stream()
                .collect(Collectors.groupingBy(ReportValidationGroupPojo::getGroupName));
        // Run through all the validators for this report
        for (Map.Entry<String, List<ReportValidationGroupPojo>> validationList : groupedByName.entrySet()) {
            List<ReportValidationGroupPojo> groupPojoList = validationList.getValue();
            ValidationType type = groupPojoList.get(0).getType();
            List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByIds(groupPojoList
                    .stream().map(ReportValidationGroupPojo::getReportControlId).collect(Collectors.toList()));
            List<InputControlPojo> inputControlPojoList = controlApi.selectMultiple(reportControlsPojoList.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
            List<String> paramValues = new ArrayList<>();
            List<String> displayValues = new ArrayList<>();

            inputControlPojoList.forEach(i -> {
                paramValues.add(params.get(i.getParamName()));
                displayValues.add(i.getDisplayName());
            });
            switch (type) {
                case NON_MANDATORY:
                    break;
                case SINGLE_MANDATORY:
                    singleMandatoryValidator.validate(displayValues, paramValues, reportPojo.getName(), groupPojoList.get(0).getValidationValue());
                    break;
                case MANDATORY:
                    mandatoryValidator.validate(displayValues, paramValues, reportPojo.getName(), groupPojoList.get(0).getValidationValue());
                    break;
                case DATE_RANGE:
                    dateValidator.validate(displayValues, paramValues, reportPojo.getName(), groupPojoList.get(0).getValidationValue());
                    break;
                default:
                    throw new ApiException(ApiStatus.BAD_DATA, "Invalid Validation Type");
            }
        }

    }

}
