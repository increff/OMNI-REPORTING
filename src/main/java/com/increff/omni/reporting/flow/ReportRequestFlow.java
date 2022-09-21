package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.api.ReportRequestApi;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
public class ReportRequestFlow extends AbstractApi {

    @Autowired
    private ReportRequestApi api;

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private InputControlApi controlApi;

    @Autowired
    private ReportControlsApi reportControlsApi;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;


    public void requestReport(ReportRequestPojo pojo, Map<String, String> params) throws ApiException {
        validate(pojo, params);
        api.add(pojo);
    }



    private void validate(ReportRequestPojo pojo, Map<String, String> params) throws ApiException {
        reportApi.getCheck(pojo.getReportId());

        //TODO check reports for which this org + user has access

        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if(!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() > MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");

        /*TODO Run through all the validators for this report*/

    }


}
