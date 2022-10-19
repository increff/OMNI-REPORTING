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
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportRequestFlowApi extends AbstractApi {

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
    @Autowired
    private ReportExpressionApi reportExpressionApi;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;

    public void requestReport(ReportRequestPojo pojo, Map<String, String> params, List<ReportInputParamsPojo> reportInputParamsPojoList) throws ApiException {
        validate(pojo, params);
        List<ReportExpressionPojo> reportExpressionPojoList = reportExpressionApi.getAllByReportId(pojo.getReportId());
        evaluateExpressions(reportExpressionPojoList, reportInputParamsPojoList, params);
        api.add(pojo);
        reportInputParamsPojoList.forEach(r -> r.setReportRequestId(pojo.getId()));
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

    public String runExpression(String expression, Map<String, String> params) throws ApiException {
        try {
            String fExpression = StringSubstitutor.replace(expression, params);

            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");
            return engine.eval(fExpression).toString();
        } catch (ScriptException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error while running expression : " + e.getMessage());
        }
    }

    private void evaluateExpressions(List<ReportExpressionPojo> reportExpressionPojoList, List<ReportInputParamsPojo> reportInputParamsPojoList, Map<String, String> params) throws ApiException {
        for (ReportExpressionPojo e : reportExpressionPojoList) {
            try {
                String result = runExpression(e.getExpression(), params);
                // Add expression also in final param list
                ReportInputParamsPojo pojo = new ReportInputParamsPojo();
                pojo.setParamKey(e.getExpressionName());
                pojo.setParamValue(result);
                reportInputParamsPojoList.add(pojo);
            } catch (Exception ex) {
                throw new ApiException(ApiStatus.BAD_DATA, "Expression evaluation failed, name : " + e.getExpressionName()
                    + " reason : " + ex.getMessage());
            }
        }
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
            runValidators(reportPojo, groupPojoList, type, paramValues, displayValues);
        }

    }

    private void runValidators(ReportPojo reportPojo, List<ReportValidationGroupPojo> groupPojoList, ValidationType type, List<String> paramValues, List<String> displayValues) throws ApiException {
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
