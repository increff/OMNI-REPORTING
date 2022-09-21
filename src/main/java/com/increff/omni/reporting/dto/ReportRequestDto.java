package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.flow.ReportRequestFlow;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportRequestDto extends AbstractDtoApi {

    @Autowired
    private ReportRequestFlow flow;

    public void requestReport(ReportRequestForm form) throws ApiException {
        checkValid(form);


        //TODO populate correctly
        Map<String, String> params  = new HashMap<>();
        ReportRequestPojo pojo = new ReportRequestPojo();

        flow.requestReport(pojo, params);

    }

}
