package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportInputParamsDao;
import com.increff.omni.reporting.pojo.ReportInputParamsPojo;
import com.nextscm.commons.spring.server.AbstractApi;
import org.hibernate.validator.constraints.LuhnCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportInputParamsApi extends AbstractApi {

    @Autowired
    private ReportInputParamsDao dao;

    public void add(List<ReportInputParamsPojo> pojos) {
        pojos.forEach(p -> dao.persist(p));
    }

    public List<ReportInputParamsPojo> getInputParamsForReportRequest(Integer reportRequestId) {
        return dao.selectMultiple("reportRequestId", reportRequestId);
    }

    public List<ReportInputParamsPojo> getInputParamsForReportRequestIds(List<Integer> reportRequestIds) {
        return dao.selectByRequestIds(reportRequestIds);
    }
}
