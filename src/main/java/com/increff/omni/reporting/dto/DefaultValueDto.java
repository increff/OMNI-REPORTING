package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.model.data.DefaultValueData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DefaultValueForm;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import io.swagger.models.auth.In;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j
@Setter
public class DefaultValueDto extends AbstractDto {

    @Autowired
    private DefaultValueApi api;
    @Autowired
    private DashboardApi dashboardApi;
    @Autowired
    private DashboardDto dashboardDto;
    @Autowired
    private DashboardChartApi dashboardChartApi;

    @Transactional(rollbackFor = ApiException.class)
    public List<DefaultValueData> upsert(List<DefaultValueForm> forms) throws ApiException {
        List<DefaultValuePojo> pojos = new ArrayList<>();
        for(DefaultValueForm form : forms) {
            checkValid(form);
            dashboardApi.getCheck(form.getDashboardId(), getOrgId());
            validateControlIdExistsForDashboard(form.getDashboardId(), form.getControlId());

            DefaultValuePojo pojo = ConvertUtil.convert(form, DefaultValuePojo.class);
            pojo.setDefaultValue(String.join(",", form.getDefaultValue()));
            pojos.add(api.upsert(pojo));
        }
        return ConvertUtil.convert(pojos, DefaultValueData.class);
    }

    private void validateControlIdExistsForDashboard(Integer dashboardId, Integer controlId) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = dashboardDto.getFilterDetails(dashboardApi.getCheck(dashboardId, getOrgId()),
                dashboardChartApi.getByDashboardId(dashboardId));
        if(filterDetails.values().stream().flatMap(List::stream).noneMatch(inputControlData -> inputControlData.getId().equals(controlId))){
            throw new ApiException(ApiStatus.BAD_DATA, "Control Id does not exist for dashboard id: " + dashboardId + " control id: " + controlId);
        }
    }



}
