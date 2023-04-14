package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if (!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() >= MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        validate(reportPojo, reportInputParamsPojoList);
        requestReportWithoutValidation(pojo, reportInputParamsPojoList);
    }

    public void requestReportWithoutValidation(ReportRequestPojo pojo,
                                               List<ReportInputParamsPojo> reportInputParamsPojoList) {
        api.add(pojo);
        reportInputParamsPojoList.forEach(r -> r.setReportRequestId(pojo.getId()));
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

}
