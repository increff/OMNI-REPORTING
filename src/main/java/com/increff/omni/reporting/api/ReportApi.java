package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportApi extends AbstractApi {

    @Autowired
    private ReportDao dao;


    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }




}
