package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportControlsDao;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportControlsApi extends AbstractApi {

    @Autowired
    private ReportControlsDao dao;

    public void add(ReportControlsPojo pojo) throws ApiException {
        ReportControlsPojo existing = getByReportAndControlId(pojo.getReportId(), pojo.getControlId());
        checkNull(existing, "Report control already exists with control id : " + pojo.getControlId());
        dao.persist(pojo);
    }

    public List<ReportControlsPojo> getByReportId(Integer reportId) {
        return dao.selectMultiple("reportId", reportId);
    }

    public List<ReportControlsPojo> getByIds(List<Integer> ids) {
        return dao.selectByIds(ids);
    }

    public ReportControlsPojo getCheck(Integer id) throws ApiException {
        ReportControlsPojo pojo = dao.select(id);
        checkNotNull(pojo, "Report control does not exist for id : " + id);
        return pojo;
    }

    public ReportControlsPojo getByReportAndControlId(Integer reportId, Integer controlId) {
        return dao.select(reportId, controlId);
    }

    public void delete(Integer id) {
        dao.remove(id);
    }
}
