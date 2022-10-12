package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportRequestDao;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportRequestApi extends AbstractApi {

    @Autowired
    private ReportRequestDao dao;

    public ReportRequestPojo add(ReportRequestPojo pojo) {
        dao.persist(pojo);
        return pojo;
    }

    public List<ReportRequestPojo> getPendingByUserId(Integer userId) {
        return dao.getByUserIdAndStatuses(userId,
                Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.IN_PROGRESS));
    }

    public ReportRequestPojo getCheck(Integer id) throws ApiException {
        ReportRequestPojo pojo = dao.select(id);
        checkNotNull(pojo, "Report request not present with id : " + id);
        return pojo;
    }

    public List<ReportRequestPojo> getEligibleRequests() {
        return dao.getEligibleReports(Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.STUCK));
    }

    public void markProcessingIfEligible(Integer id) {
        ReportRequestPojo pojo = dao.select(id);
        if (pojo.getStatus().equals(ReportRequestStatus.NEW) ||
                (pojo.getStatus().equals(ReportRequestStatus.STUCK))) {
            pojo.setStatus(ReportRequestStatus.IN_PROGRESS);
        }
    }

    public void markStuck(Integer stuckReportTime) {
        List<ReportRequestPojo> stuck = dao.getStuckReports(stuckReportTime);
        stuck.forEach(s -> {
            s.setStatus(ReportRequestStatus.STUCK);
        });
    }

    public void updateStatus(Integer id, ReportRequestStatus status, String filePath) throws ApiException {
        ReportRequestPojo reportRequestPojo = getCheck(id);
        reportRequestPojo.setStatus(status);
        reportRequestPojo.setUrl(filePath);
    }

    public List<ReportRequestPojo> getByUserId(int userId, Integer days) {
        return dao.selectByUserIdAndDays(userId, days);
    }
}
