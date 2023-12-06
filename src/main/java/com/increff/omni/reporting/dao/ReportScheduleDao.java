package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.ScheduleStatus;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class ReportScheduleDao extends AbstractDao<ReportSchedulePojo> {

    public List<ReportSchedulePojo> getEligibleSchedules() {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        query.where(
                cb.and(
                        cb.isTrue(root.get("isEnabled")),
                        cb.isFalse(root.get("isDeleted")),
                        cb.lessThanOrEqualTo(root.get("nextRuntime"), ZonedDateTime.now())),
                        cb.equal(root.get("status"), ScheduleStatus.NEW)
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.getResultList();
    }

    public List<ReportSchedulePojo> getEligibleSchedulesById(Integer id) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        query.where(
                cb.and(
                        cb.isTrue(root.get("isEnabled")),
                        cb.isFalse(root.get("isDeleted")),
                        cb.lessThanOrEqualTo(root.get("nextRuntime"), ZonedDateTime.now())),
                cb.equal(root.get("status"), ScheduleStatus.NEW),
                cb.equal(root.get("id"), id)
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.getResultList();
    }

    public List<ReportSchedulePojo> getStuckSchedules(Integer stuckScheduleSeconds) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        query.where(
                cb.and(
                        cb.isTrue(root.get("isEnabled")),
                        cb.isFalse(root.get("isDeleted")),
                        cb.lessThanOrEqualTo(root.get("updatedAt"), ZonedDateTime.now().minusSeconds(stuckScheduleSeconds))),
                        cb.equal(root.get("status"), ScheduleStatus.RUNNING)
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.getResultList();
    }

    public List<ReportSchedulePojo> selectByOrgId(Integer orgId, Boolean isEnabled, Integer pageNo, Integer pageSize) {
        if(Objects.isNull(pageNo))
            pageNo = 1;
        if(Objects.isNull(pageSize) || pageSize > 100)
            pageSize = 100;
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(orgId))
            predicates.add(cb.equal(root.get("orgId"), orgId));
        predicates.add(cb.isFalse(root.get("isDeleted")));
        if (Objects.nonNull(isEnabled))
            predicates.add(cb.equal(root.get("isEnabled"), isEnabled));
        query.where(
                cb.and(predicates.toArray(new Predicate[0]))
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.setFirstResult((pageNo - 1) * pageSize)
                .setMaxResults(pageSize).getResultList();
    }

    public List<ReportSchedulePojo> selectByOrgIdReportAlias(List<Integer> orgIds, String reportAlias) {
        if(orgIds.isEmpty())return new ArrayList<>();
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(root.get("orgId").in(orgIds));
        predicates.add(cb.equal(root.get("reportAlias"), reportAlias));
        predicates.add(cb.isFalse(root.get("isDeleted")));
        query.where(
                cb.and(predicates.toArray(new Predicate[0]))
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.getResultList();
    }
}
