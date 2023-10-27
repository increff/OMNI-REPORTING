package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class ReportDao extends AbstractDao<ReportPojo> {

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isReport) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isReport"), isReport)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isReport) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("name"), name),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isReport"), isReport)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isReport) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isReport"), isReport)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isReport) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("isReport"), isReport)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public ReportPojo getByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isReport) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("alias"), alias),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isReport"), isReport)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }
}
