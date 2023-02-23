package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.dao.ReportScheduleEmailsDao;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportScheduleApi extends AbstractAuditApi {

    @Autowired
    private ReportScheduleDao dao;
    @Autowired
    private ReportScheduleEmailsDao emailsDao;

    public void add(ReportSchedulePojo pojo) {
        dao.persist(pojo);
    }

    public List<ReportSchedulePojo> selectByOrgIdAndEnabledStatus(Integer orgId, Boolean isEnabled, Integer pageNo,
                                                                  Integer pageSize) {
        return dao.selectByOrgId(orgId, isEnabled, pageNo, pageSize);
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
        ex.setReportName(pojo.getReportName());
        ex.setCron(pojo.getCron());
        ex.setUserId(pojo.getUserId());
        ex.setIsEnabled(pojo.getIsEnabled());
        ex.setTimezone(pojo.getTimezone());
        ex.setNextRuntime(pojo.getNextRuntime());
        dao.update(ex);
    }

    public void addEmails(List<ReportScheduleEmailsPojo> emailsPojos) {
        emailsPojos.forEach(e -> emailsDao.persist(e));
    }

    public List<ReportScheduleEmailsPojo> getByScheduleId(Integer scheduleId) {
        return emailsDao.selectMultiple("scheduleId", scheduleId);
    }

    public void removeExistingEmails(Integer scheduleId) {
        List<ReportScheduleEmailsPojo> existingEmails = getByScheduleId(scheduleId);
        existingEmails.forEach(e -> emailsDao.remove(e.getId()));
        emailsDao.flush();
    }
}
