package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
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

    public List<ReportRequestPojo> getByUserIdAndStatuses(ReportRequestType type, Integer userId,
                                                          List<ReportRequestStatus> statuses) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        root.get("status").in(statuses),
                        cb.equal(root.get("userId"), userId)
                )
        );
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportRequestPojo> getEligibleReports(List<ReportRequestType> type, List<ReportRequestStatus> statuses,
                                                      int limitForEligibleRequest) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(root.get("type").in(type),
                        root.get("status").in(statuses))
        ).orderBy(cb.asc(root.get("createdAt")));
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

    public List<ReportRequestPojo> selectByUserId(int userId, ReportRequestType type, Integer limit) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.equal(root.get("userId"), userId))
        ).orderBy(cb.desc(root.get("createdAt")));
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return tQuery.setMaxResults(limit).getResultList();
    }

    public List<ReportRequestPojo> getByOrgAndType(Integer orgId, ReportRequestType type, Integer pageNo,
                                                   Integer pageSize) {
        if(Objects.isNull(pageNo))
            pageNo = 1;
        if(Objects.isNull(pageSize))
            pageSize = 100;
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportRequestPojo> query = cb.createQuery(ReportRequestPojo.class);
        Root<ReportRequestPojo> root = query.from(ReportRequestPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.equal(root.get("orgId"), orgId))
        ).orderBy(cb.desc(root.get("createdAt")));
        TypedQuery<ReportRequestPojo> tQuery = createQuery(query);
        return tQuery.setFirstResult((pageNo - 1) * pageSize)
                .setMaxResults(pageSize).getResultList();
    }
}
