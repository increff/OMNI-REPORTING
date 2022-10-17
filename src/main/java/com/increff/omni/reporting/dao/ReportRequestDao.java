package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@Transactional
public class ReportRequestDao extends AbstractDao<ReportRequestPojo> {

    private static final String selectByUserIdAndStatus = "SELECT r FROM ReportRequestPojo r" //
            + " WHERE r.userId = :userId and r.status IN :statuses";

    private static final String selectStuckReports = "SELECT r FROM ReportRequestPojo r " +
            "where status = 'IN_PROGRESS' and date_add(updatedAt,interval :stuckReportTime minute)<NOW()" +
            "order by createdAt desc limit 100";

    public List<ReportRequestPojo> getByUserIdAndStatuses(Integer userId, List<ReportRequestStatus> statuses) {
        TypedQuery<ReportRequestPojo> q = createJpqlQuery(selectByUserIdAndStatus);
        q.setParameter("userId", userId);
        q.setParameter("statuses", statuses);
        return selectMultiple(q);
    }

    public List<ReportRequestPojo> getEligibleReports(List<ReportRequestStatus> statuses, int limitForEligibleRequest) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                root.get("status").in(statuses)
        ).orderBy(cb.desc(root.get("createdAt")));
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return tQuery.setMaxResults(limitForEligibleRequest).getResultList();
    }

    public List<ReportRequestPojo> getStuckReports(Integer stuckReportTime) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("status"), ReportRequestStatus.IN_PROGRESS),
                        cb.lessThanOrEqualTo(root.get("updatedAt"), ZonedDateTime.now().minusMinutes(stuckReportTime))
                )
        ).orderBy(cb.desc(root.get("createdAt")));
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return tQuery.setMaxResults(100).getResultList();
    }

    public List<ReportRequestPojo> selectByUserId(int userId, Integer limit) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("userId"), userId)
                )
        ).orderBy(cb.desc(root.get("createdAt")));
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return tQuery.setMaxResults(limit).getResultList();
    }
}
