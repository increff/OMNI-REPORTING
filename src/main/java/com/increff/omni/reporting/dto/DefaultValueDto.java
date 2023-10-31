package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.model.data.DefaultValueData;
import com.increff.omni.reporting.model.form.DefaultValueForm;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import io.swagger.models.auth.In;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j
@Setter
public class DefaultValueDto extends AbstractDto {

    @Autowired
    private DefaultValueApi api;
    @Autowired
    private DashboardApi dashboardApi;

    public List<DefaultValueData> upsert(List<DefaultValueForm> forms) throws ApiException {
        List<DefaultValuePojo> pojos = new ArrayList<>();
        for(DefaultValueForm form : forms) {
            checkValid(form);
            dashboardApi.getCheck(form.getDashboardId(), getOrgId());
            // TODO: Check if control id exists for that dashboard id
            DefaultValuePojo pojo = ConvertUtil.convert(form, DefaultValuePojo.class);
            pojo.setDefaultValue(String.join(",", form.getDefaultValue()));
            pojos.add(api.upsert(pojo));
        }
        return ConvertUtil.convert(pojos, DefaultValueData.class);
    }



}
