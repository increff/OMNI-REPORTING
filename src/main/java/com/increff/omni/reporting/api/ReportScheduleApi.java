package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.dao.ReportScheduleEmailsDao;
import com.increff.omni.reporting.dao.ReportScheduleInputParamsDao;
import com.increff.omni.reporting.model.constants.ScheduleStatus;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j
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

    public List<ReportSchedulePojo> selectByOrgIdReportAlias(List<Integer> orgIds, String reportAlias) {
        return dao.selectByOrgIdReportAlias(orgIds, reportAlias);
    }

    public List<ReportSchedulePojo> getEligibleSchedules() {
        return dao.getEligibleSchedules();
    }

    public ReportSchedulePojo getCheckEligibleSchedulesById(Integer id) throws ApiException {
        List<ReportSchedulePojo> pojos = dao.getEligibleSchedulesById(id);
        if(pojos.size() > 1)
            throw new ApiException(ApiStatus.BAD_DATA, "More than one eligible schedule found for id : " + id);
        if(pojos.size() == 0)
            throw new ApiException(ApiStatus.BAD_DATA, "No eligible schedule found for id : " + id);
        return pojos.get(0);
    }

    public List<ReportSchedulePojo> getStuckSchedules(Integer stuckScheduleSeconds) {
        return dao.getStuckSchedules(stuckScheduleSeconds);
    }

    public ReportSchedulePojo getCheckStuckSchedulesById(Integer stuckScheduleSeconds, Integer id) throws ApiException {
        List<ReportSchedulePojo> pojos = dao.getStuckSchedulesById(stuckScheduleSeconds, id);
        if(pojos.size() > 1)
            throw new ApiException(ApiStatus.BAD_DATA, "More than one stuck schedule found for id : " + id);
        if(pojos.size() == 0)
            throw new ApiException(ApiStatus.BAD_DATA, "No stuck schedule found for id : " + id);
        return pojos.get(0);
    }

    public void updateStatusToRunning(Integer id) throws ApiException {
        ReportSchedulePojo pojo = getCheckEligibleSchedulesById(id);
        if(pojo.getStatus()!=(ScheduleStatus.NEW)){ // This check may be redundant
            throw new ApiException(ApiStatus.BAD_DATA, "Optimistic Lock. Failed to change Schedule id:" + id +
                    " status to " + ScheduleStatus.RUNNING + "Cur Status is not " + ScheduleStatus.NEW);
        }
        pojo.setStatus(ScheduleStatus.RUNNING);
        dao.update(pojo);
    }

    public void updateStatusToNew(Integer stuckScheduleSeconds, Integer id) throws ApiException {
        ReportSchedulePojo pojo = getCheckStuckSchedulesById(stuckScheduleSeconds, id);
        if(pojo.getStatus()!=ScheduleStatus.RUNNING){
            throw new ApiException(ApiStatus.BAD_DATA, "Optimistic Lock. Failed to change Schedule id:" + id + " status to " + ScheduleStatus.NEW + "Cur Status is not " + ScheduleStatus.RUNNING);
        }
        pojo.setStatus(ScheduleStatus.NEW);
        dao.update(pojo);
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
        ex.setStatus(pojo.getStatus());
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
