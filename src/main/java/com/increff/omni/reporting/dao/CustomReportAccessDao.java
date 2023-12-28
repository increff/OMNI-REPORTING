package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
import com.increff.commons.springboot.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@Repository
@Transactional
public class CustomReportAccessDao extends AbstractDao<CustomReportAccessPojo> {

    public CustomReportAccessPojo selectByOrgAndReport(Integer reportId, Integer orgId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<CustomReportAccessPojo> query = cb.createQuery(CustomReportAccessPojo.class);
        Root<CustomReportAccessPojo> root = query.from(CustomReportAccessPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("reportId"), reportId),
                        cb.equal(root.get("orgId"), orgId)
                )
        );
        TypedQuery<CustomReportAccessPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

}
