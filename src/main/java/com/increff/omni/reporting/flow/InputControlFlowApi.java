package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.constants.InputControlScope;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InputControlFlowApi extends AbstractApi {

    @Autowired
    private InputControlApi api;

    @Autowired
    private ReportControlsApi reportControlsApi;

    @Autowired
    private ReportApi reportApi;

    public InputControlPojo addGlobalControl(InputControlPojo pojo) throws ApiException {
        validateGlobalControl(pojo);
        return api.add(pojo);
    }

    public InputControlPojo addLocalControl(Integer reportId, InputControlPojo pojo) throws ApiException {
        validateLocalControl(reportId, pojo);
        return api.add(pojo);
    }

    private void validateLocalControl(Integer reportId, InputControlPojo pojo) throws ApiException {
        reportApi.getCheck(reportId);

        //Validating if any other control exists with same display or param name
        List<ReportControlsPojo> existingPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = existingPojos.stream().map(ReportControlsPojo::getControlId)
                .collect(Collectors.toList());

        List<InputControlPojo> controlPojos = api.selectMultiple(controlIds);

        List<InputControlPojo> duplicate = controlPojos.stream()
                .filter(i -> (i.getDisplayName().equals(pojo.getDisplayName()) ||
                        i.getParamName().equals(pojo.getParamName())))
                .collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(duplicate))
            throw new ApiException(ApiStatus.BAD_DATA, "Another input control present with same display name" +
                    " or param name");
    }


    private void validateGlobalControl(InputControlPojo pojo) throws ApiException {
        InputControlPojo existingByName =
                api.getByScopeAndDisplayName(InputControlScope.GLOBAL, pojo.getDisplayName());

        InputControlPojo existingByParam =
                api.getByScopeAndParamName(InputControlScope.GLOBAL, pojo.getParamName());

        if(existingByName != null || existingByParam != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Cannot create input control with same" +
                    " display name or param name");
    }


}
