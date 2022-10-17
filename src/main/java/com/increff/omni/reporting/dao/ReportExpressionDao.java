package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportExpressionPojo;
import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Repository
@Transactional
public class ReportExpressionDao extends AbstractDao<ReportExpressionPojo> {

    public ReportExpressionPojo selectByNameAndReportId(Integer reportId, String expressionName) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportExpressionPojo> query = cb.createQuery(ReportExpressionPojo.class);
        Root<ReportExpressionPojo> root = query.from(ReportExpressionPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("reportId"), reportId),
                        cb.equal(root.get("expressionName"), expressionName)
                )
        );
        TypedQuery<ReportExpressionPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }
}
