package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportControlsDao;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportControlsApi extends AbstractApi {

    @Autowired
    private ReportControlsDao dao;

    public List<ReportControlsPojo> getByReportId(Integer reportId){
        return dao.selectMultiple("reportId", reportId);
    }

    public ReportControlsPojo add(ReportControlsPojo pojo) throws ApiException {
        ReportControlsPojo existing = select(pojo.getReportId(), pojo.getControlId());
        checkNull(existing, "This control already present for selected report");
        dao.persist(pojo);
        return pojo;
    }

    public ReportControlsPojo select(Integer reportId, Integer controlId){
        return dao.select(reportId, controlId);
    }

}
