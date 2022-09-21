package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.db.AbstractDao;
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
            "where status = 'IN_PROGRESS' and date_add(updatedAt,interval 7 minute)<NOW()" +
            "order by createdAt desc limit 100";

    public List<ReportRequestPojo> getByUserIdAndStatuses(String userId, List<ReportRequestStatus> stasuses){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectByUserIdAndStatus);
        q.setParameter("userId", userId);
        q.setParameter("statuses", stasuses);
        return selectMultiple(q);
    }

    public List<ReportRequestPojo> getEligibleReports(List<ReportRequestStatus> statuses){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectReportsByStatuses);
        q.setParameter("statuses", statuses);
        return selectMultiple(q);
    }

    public List<ReportRequestPojo> getStuckReports(){
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectStuckReports);
        return selectMultiple(q);
    }

}
