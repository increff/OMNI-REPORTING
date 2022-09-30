package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
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

import java.util.List;
import java.util.Map;

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
    private MandatoryValidator mandatoryValidator;
    @Autowired
    private SingleMandatoryValidator singleMandatoryValidator;
    @Autowired
    private DateValidator dateValidator;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;

    public void requestReport(ReportRequestPojo pojo, Map<String, String> params, List<ReportInputParamsPojo> reportInputParamsPojoList) throws ApiException {
        validate(pojo, params);
        api.add(pojo);
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

    private void validate(ReportRequestPojo pojo, Map<String, String> params) throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());

        // TODO check reports for which this org + user has access
        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if(!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() > MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(pojo.getReportId());
        // TODO Run through all the validators for this report
        for(ReportControlsPojo reportControlsPojo : reportControlsPojoList) {
            InputControlPojo inputControlPojo = controlApi.getCheck(reportControlsPojo.getControlId());
            String paramValue = params.get(inputControlPojo.getParamName());
            switch (reportControlsPojo.getValidationType()) {
                case NON_MANDATORY:
                    break;
                case SINGLE_MANDATORY:
                    singleMandatoryValidator.validate(inputControlPojo.getDisplayName(), paramValue, reportPojo.getName());
                    break;
                case MANDATORY:
                    mandatoryValidator.validate(inputControlPojo.getDisplayName(), paramValue, reportPojo.getName());
                    break;
                case DATE:
                    dateValidator.validate(inputControlPojo.getDisplayName(), paramValue, reportPojo.getName());
                    break;
                default:
                    throw new ApiException(ApiStatus.BAD_DATA, "Invalid Validation Type");
            }
        }

    }


}
