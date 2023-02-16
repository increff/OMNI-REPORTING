package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportScheduleApi extends AbstractAuditApi {

    @Autowired
    private ReportScheduleDao dao;

    public void add(ReportSchedulePojo pojo) {
        dao.persist(pojo);
    }

    public List<ReportSchedulePojo> selectByOrgId(int orgId) {
        return dao.selectByOrgId(orgId);
    }

    public ReportSchedulePojo select(Integer id) {
        return dao.select(id);
    }

    public List<ReportSchedulePojo> getEligibleSchedules() {
        return dao.getEligibleSchedules();
    }

    public ReportSchedulePojo getCheck(Integer id) throws ApiException {
        ReportSchedulePojo pojo = dao.select(id);
        checkNotNull(pojo, "No report schedule present with id : " + id);
        return pojo;
    }

    public void edit(ReportSchedulePojo pojo) throws ApiException {
        ReportSchedulePojo ex = getCheck(pojo.getId());
        ex.setReportId(pojo.getReportId());
        ex.setType(pojo.getType());
        ex.setCron(pojo.getCron());
        ex.setSendTo(pojo.getSendTo());
        ex.setUserId(pojo.getUserId());
        ex.setIsEnabled(pojo.getIsEnabled());
        ex.setTimezone(pojo.getTimezone());
        ex.setNextRuntime(pojo.getNextRuntime());
        dao.update(ex);
    }
}
