package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportControlsPojo;
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
public class ReportControlsDao extends AbstractDao<ReportControlsPojo> {

    public ReportControlsPojo select(Integer reportId, Integer controlId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportControlsPojo> query = cb.createQuery(ReportControlsPojo.class);
        Root<ReportControlsPojo> root = query.from(ReportControlsPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("reportId"), reportId),
                        cb.equal(root.get("controlId"), controlId)
                )
        );
        TypedQuery<ReportControlsPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportControlsPojo> selectByIds(List<Integer> reportControlIds) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportControlsPojo> query = cb.createQuery(ReportControlsPojo.class);
        Root<ReportControlsPojo> root = query.from(ReportControlsPojo.class);
        query.where(
                root.get("id").in(reportControlIds)
        );
        TypedQuery<ReportControlsPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
