package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.commons.AbstractDao;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
//import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
                        cb.lessThanOrEqualTo(root.get("nextRuntime"), ZonedDateTime.now()))
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
}
