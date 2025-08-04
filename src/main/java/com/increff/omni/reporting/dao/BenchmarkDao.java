package com.increff.omni.reporting.dao;

import com.increff.commons.springboot.db.dao.AbstractDao;
import com.increff.omni.reporting.pojo.BenchmarkPojo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;

@Repository
@Transactional
public class BenchmarkDao extends AbstractDao<BenchmarkPojo> {

    private static final String SELECT_BY_REPORT_ID = "SELECT p FROM BenchmarkPojo p WHERE p.reportId=:reportId";

    public BenchmarkPojo selectByReportId(Integer reportId) {
        TypedQuery<BenchmarkPojo> query = em.createQuery(SELECT_BY_REPORT_ID, BenchmarkPojo.class);
        query.setParameter("reportId", reportId);
        return selectSingleOrNull(query);
    }

    public void add(BenchmarkPojo pojo) {
        em.persist(pojo);
    }
}
