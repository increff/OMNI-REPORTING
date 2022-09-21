package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

@Repository
@Transactional
public class ReportControlsDao extends AbstractDao<ReportControlsPojo> {

    private static final String selectByReportAndControl = "SELECT r FROM ReportControlsPojo r" //
            + " WHERE r.reportId = :reportId and r.controlId = :controlId";

    public ReportControlsPojo select(Integer reportId, Integer controlId){
        TypedQuery<ReportControlsPojo> q = createJpqlQuery(selectByReportAndControl);
        q.setParameter("reportId", reportId);
        q.setParameter("controlId", controlId);
        return selectSingleOrNull(q);
    }

}
