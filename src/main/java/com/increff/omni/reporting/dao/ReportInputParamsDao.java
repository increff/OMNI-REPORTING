package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportInputParamsPojo;
//import com.nextscm.commons.spring.db.AbstractDao;
import com.increff.omni.reporting.commons.AbstractDao;
import org.springframework.stereotype.Repository;

//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Root;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
public class ReportInputParamsDao extends AbstractDao<ReportInputParamsPojo> {

    public List<ReportInputParamsPojo> selectByRequestIds(List<Integer> reportRequestIds) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportInputParamsPojo> query = cb.createQuery(ReportInputParamsPojo.class);
        Root<ReportInputParamsPojo> root = query.from(ReportInputParamsPojo.class);
        query.where(
                root.get("reportRequestId").in(reportRequestIds)
        );
        TypedQuery<ReportInputParamsPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
