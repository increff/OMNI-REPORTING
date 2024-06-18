package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportRequestDao;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.server.AbstractApi;
import com.increff.omni.reporting.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.util.ConstantsUtil.MAX_RETRY_COUNT;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportRequestApi extends AbstractApi {

    @Autowired
    private ReportRequestDao dao;

    public ReportRequestPojo add(ReportRequestPojo pojo) {
        dao.persist(pojo);
        return pojo;
    }

    public List<ReportRequestPojo> getPendingByUserId(Integer userId) {
        return dao.getByUserIdAndStatuses(ReportRequestType.USER, userId,
                Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.IN_PROGRESS, ReportRequestStatus.REQUESTED));
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

    public void updateStatus(Integer id, ReportRequestStatus status, String filePath, Integer noOfRows, Double fileSize,
                             String failureReason, ZonedDateTime completionTime)
            throws ApiException {
        ReportRequestPojo reportRequestPojo = getCheck(id);
        reportRequestPojo.setStatus(status);
        reportRequestPojo.setUrl(filePath);
        reportRequestPojo.setFileSize(fileSize);
        reportRequestPojo.setNoOfRows(noOfRows);
        reportRequestPojo.setFailureReason(failureReason);
        reportRequestPojo.setDisplayFailureReason(ConvertUtil.getDisplayFailureReason(failureReason));
        if (reportRequestPojo.getStatus().equals(ReportRequestStatus.COMPLETED) ||
                reportRequestPojo.getStatus().equals(ReportRequestStatus.FAILED))
            reportRequestPojo.setRequestCompletionTime(completionTime);
        dao.update(reportRequestPojo);
    }

    public List<ReportRequestPojo> getByUserId(int userId, Integer limit) {
        return dao.selectByUserId(userId, ReportRequestType.USER, limit);
    }

    public void markFailed(Integer id, ReportRequestStatus status, String message, int noOfRows, double fileSize)
            throws ApiException {
        ReportRequestPojo reportRequestPojo = getCheck(id);

        if (reportRequestPojo.getType().equals(ReportRequestType.EMAIL)) {
            reportRequestPojo.setRetryCount(reportRequestPojo.getRetryCount() + 1);
            if (reportRequestPojo.getRetryCount() < MAX_RETRY_COUNT) {
                status = ReportRequestStatus.NEW;
            }
        }

        reportRequestPojo.setStatus(status);
        reportRequestPojo.setFailureReason(message);
        reportRequestPojo.setDisplayFailureReason(ConvertUtil.getDisplayFailureReason(message));
        reportRequestPojo.setFileSize(fileSize);
        reportRequestPojo.setNoOfRows(noOfRows);
        reportRequestPojo.setRequestCompletionTime(ZonedDateTime.now());
        dao.update(reportRequestPojo);
    }

    public List<ReportRequestPojo> getStuckRequests(Integer stuckReportTime) {
        return dao.getStuckReports(stuckReportTime);
    }

    public List<ReportRequestPojo> getPendingRequests() {
        return dao.getPendingRequests();
    }

    public List<ReportRequestPojo> getByOrgAndType(Integer orgId, ReportRequestType type, Integer pageNo,
                                                   Integer pageSize) {
        return dao.getByOrgAndType(orgId, type, pageNo, pageSize);
    }

    public List<ReportRequestPojo> getByIds(List<Integer> reportRequestIds) {
        return dao.selectByIds(reportRequestIds);
    }
}
