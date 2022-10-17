package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;

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
