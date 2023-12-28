package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.increff.commons.springboot.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class ReportValidationGroupDao extends AbstractDao<ReportValidationGroupPojo> {

    public List<ReportValidationGroupPojo> selectByNameAndId(Integer reportId, String groupName) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportValidationGroupPojo> query = cb.createQuery(ReportValidationGroupPojo.class);
        Root<ReportValidationGroupPojo> root = query.from(ReportValidationGroupPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("reportId"), reportId),
                        cb.equal(root.get("groupName"), groupName)
                )
        );
        TypedQuery<ReportValidationGroupPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportValidationGroupPojo> selectByIdAndControlId(Integer reportId, Integer reportControlId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportValidationGroupPojo> query = cb.createQuery(ReportValidationGroupPojo.class);
        Root<ReportValidationGroupPojo> root = query.from(ReportValidationGroupPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("reportId"), reportId),
                        cb.equal(root.get("reportControlId"), reportControlId)
                )
        );
        TypedQuery<ReportValidationGroupPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
