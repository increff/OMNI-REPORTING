package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;


@Repository
@Transactional
public class ChartLegendsDao extends AbstractDao<ChartLegendsPojo> {

    private static final String SELECT_BY_CHART_ID = "SELECT o FROM ChartLegendsPojo o WHERE o.chartId=:chartId";
    public List<ChartLegendsPojo> getByChartId(Integer chartId) {
        TypedQuery<ChartLegendsPojo> q = createJpqlQuery(SELECT_BY_CHART_ID);
        q.setParameter("chartId", chartId);
        return q.getResultList();
    }

}
