package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportQueryDao;
import com.increff.omni.reporting.pojo.ReportQueryPojo;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportQueryApi extends AbstractApi {

    @Autowired
    private ReportQueryDao dao;

    public ReportQueryPojo upsertQuery(ReportQueryPojo pojo){
        ReportQueryPojo existing = getByReportId(pojo.getReportId());
        if(existing == null){
            dao.persist(pojo);
            return pojo;
        }
        else{
            existing.setQuery(pojo.getQuery());
            dao.update(existing);
            return existing;
        }
    }

    public ReportQueryPojo getByReportId(Integer reportId) {
        return dao.select("reportId",reportId);
    }

}
