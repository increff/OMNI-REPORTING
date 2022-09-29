package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class ReportRequestDao extends AbstractDao<ReportRequestPojo> {

    private static final String selectByUserIdAndStatus = "SELECT r FROM ReportRequestPojo r" //
            + " WHERE r.userId = :userId and r.status IN :statuses";

    private static final String selectReportsByStatuses = "SELECT r FROM ReportRequestPojo r " +
            "where status IN :statuses" +
            "order by createdAt desc limit 100";

    private static final String selectStuckReports = "SELECT r FROM ReportRequestPojo r " +
            "where status = 'IN_PROGRESS' and date_add(updatedAt,interval :stuckReportTime minute)<NOW()" +
            "order by createdAt desc limit 100";

    public List<ReportRequestPojo> getByUserIdAndStatuses(Integer userId, List<ReportRequestStatus> statuses){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectByUserIdAndStatus);
        q.setParameter("userId", userId);
        q.setParameter("statuses", statuses);
        return selectMultiple(q);
    }

    public List<ReportRequestPojo> getEligibleReports(List<ReportRequestStatus> statuses){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectReportsByStatuses);
        q.setParameter("statuses", statuses);
        return selectMultiple(q);
    }

    public List<ReportRequestPojo> getStuckReports(Integer stuckReportTime){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectStuckReports);
        q.setParameter("stuckReportTime", stuckReportTime);
        return selectMultiple(q);
    }

}
