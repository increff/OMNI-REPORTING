package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
* This class will make sure that resource based access control is ensured
* */

//TODO this class is just for auth related validation etc. If we feel in last that this is not needed. We can remove

@Service
public class UserDto extends AbstractDtoApi {
    // Todo how BI team will have access to do admin operations as we will have only one deployment around it?
    @Autowired
    private ReportRequestDto reportRequestDto;

    public void requestReport(ReportRequestForm form) throws ApiException {
        //Some validation around auth if needed
        reportRequestDto.requestReport(form);
    }
}
