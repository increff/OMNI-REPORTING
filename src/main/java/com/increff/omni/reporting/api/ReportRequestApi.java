package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportRequestDao;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return dao.getByUserIdAndStatuses(ReportRequestType.USER, userId,
                Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.IN_PROGRESS));
    }

    public ReportRequestPojo getCheck(Integer id) throws ApiException {
        ReportRequestPojo pojo = dao.select(id);
        checkNotNull(pojo, "Report request not present with id : " + id);
        return pojo;
    }

    public List<ReportRequestPojo> getEligibleRequests(List<ReportRequestType> type, int limitForEligibleRequest) {
        if (limitForEligibleRequest <= 0)
            return new ArrayList<>();
        return dao.getEligibleReports(type, Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.STUCK)
                , limitForEligibleRequest);
    }

    public void markProcessingIfEligible(Integer id) throws ApiException {
        ReportRequestPojo pojo = dao.select(id);
        if (pojo.getStatus().equals(ReportRequestStatus.NEW) ||
                (pojo.getStatus().equals(ReportRequestStatus.STUCK))) {
            pojo.setStatus(ReportRequestStatus.IN_PROGRESS);
        } else
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Task not in eligible state");
    }

    public void markStuck(ReportRequestPojo s) {
        ReportRequestPojo p = dao.select(s.getId());
        p.setStatus(ReportRequestStatus.STUCK);
    }

    public void updateStatus(Integer id, ReportRequestStatus status, String filePath, Integer noOfRows, Double fileSize)
            throws ApiException {
        ReportRequestPojo reportRequestPojo = getCheck(id);
        reportRequestPojo.setStatus(status);
        reportRequestPojo.setUrl(filePath);
        reportRequestPojo.setFileSize(fileSize);
        reportRequestPojo.setNoOfRows(noOfRows);
        dao.update(reportRequestPojo);
    }

    public List<ReportRequestPojo> getByUserId(int userId, Integer limit) {
        return dao.selectByUserId(userId, ReportRequestType.USER, limit);
    }

    public void markFailed(Integer id, ReportRequestStatus status, String message, int noOfRows, double fileSize)
            throws ApiException {
        ReportRequestPojo reportRequestPojo = getCheck(id);
        reportRequestPojo.setStatus(status);
        reportRequestPojo.setFailureReason(message);
        reportRequestPojo.setFileSize(fileSize);
        reportRequestPojo.setNoOfRows(noOfRows);
        dao.update(reportRequestPojo);
    }

    public List<ReportRequestPojo> getStuckRequests(Integer stuckReportTime) {
        return dao.getStuckReports(stuckReportTime);
    }

    public List<ReportRequestPojo> getByOrg(int orgId, ReportRequestType type) {
        // todo
        return new ArrayList<>();
    }
}
