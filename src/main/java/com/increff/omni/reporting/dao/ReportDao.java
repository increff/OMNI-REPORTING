package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.commons.AbstractDao;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.ReportPojo;
//import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class ReportDao extends AbstractDao<ReportPojo> {

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isDashboard) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isDashboard"), isDashboard)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isDashboard) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("name"), name),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isDashboard"), isDashboard)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isDashboard) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isDashboard"), isDashboard)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isDashboard) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("isDashboard"), isDashboard)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
