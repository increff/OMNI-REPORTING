package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class ReportScheduleDao extends AbstractDao<ReportSchedulePojo> {

    public List<ReportSchedulePojo> getEligibleSchedules() {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportSchedulePojo> query = cb.createQuery(ReportSchedulePojo.class);
        Root<ReportSchedulePojo> root = query.from(ReportSchedulePojo.class);
        query.where(
                cb.and(
                        cb.isTrue(root.get("enabled")),
                        cb.greaterThanOrEqualTo(root.get("nextRuntime"), ZonedDateTime.now()))
        );
        TypedQuery<ReportSchedulePojo> tQuery = createQuery(query);
        return tQuery.getResultList();
    }
}
