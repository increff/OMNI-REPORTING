package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.dao.ReportScheduleEmailsDao;
import com.increff.omni.reporting.dao.ReportScheduleInputParamsDao;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
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
    @Autowired
    private ReportScheduleInputParamsDao scheduleInputParamsDao;

    public void add(ReportSchedulePojo pojo) {
        dao.persist(pojo);
    }

    public void addScheduleInputParams(List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojoList,
                                       ReportSchedulePojo schedulePojo) {
        reportScheduleInputParamsPojoList.forEach(r -> {
            r.setScheduleId(schedulePojo.getId());
            scheduleInputParamsDao.persist(r);
        });
    }

    public List<ReportScheduleInputParamsPojo> getScheduleParams(Integer scheduleId) {
        return scheduleInputParamsDao.selectMultiple("scheduleId", scheduleId);
    }

    public void updateScheduleInputParams(List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojoList,
                                          ReportSchedulePojo schedulePojo) {
        List<ReportScheduleInputParamsPojo> existing = getScheduleParams(schedulePojo.getId());
        existing.forEach(e -> scheduleInputParamsDao.remove(e.getId()));
        scheduleInputParamsDao.flush();
        reportScheduleInputParamsPojoList.forEach(r -> {
            r.setScheduleId(schedulePojo.getId());
            scheduleInputParamsDao.persist(r);
        });
    }

    public List<ReportSchedulePojo> selectByOrgIdAndEnabledStatus(Integer orgId, Boolean isEnabled, Integer pageNo,
                                                                  Integer pageSize) {
        return dao.selectByOrgId(orgId, isEnabled, pageNo, pageSize);
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
        ex.setReportAlias(pojo.getReportAlias());
        ex.setCron(pojo.getCron());
        ex.setUserId(pojo.getUserId());
        ex.setIsEnabled(pojo.getIsEnabled());
        ex.setNextRuntime(pojo.getNextRuntime());
        ex.setIsDeleted(pojo.getIsDeleted());
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

    public void addScheduleCount(Integer scheduleId, Integer successCount, Integer failureCount) {
        ReportSchedulePojo pojo = dao.select(scheduleId);
        pojo.setSuccessCount(pojo.getSuccessCount() + successCount);
        pojo.setFailureCount(pojo.getFailureCount() + failureCount);
    }
}
