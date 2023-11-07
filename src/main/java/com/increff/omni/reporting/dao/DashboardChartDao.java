package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class DashboardChartDao extends AbstractDao<DashboardChartPojo> {

    public final String SELECT_BY_DASHBOARD_ID = "select p from DashboardChartPojo p where p.dashboardId = :id";
    public List<DashboardChartPojo> getByDashboardId(Integer id) {
        return selectMultiple(createJpqlQuery(SELECT_BY_DASHBOARD_ID)
                .setParameter("id", id));
    }

    public final String SELECT_BY_DASHBOARD_AND_CHART_ALIAS = "select p from DashboardChartPojo p where p.dashboardId = :dashboardId and p.chartAlias = :chartAlias";
    public DashboardChartPojo getByDashboardAndChartAlias(Integer dashboardId, String chartAlias) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_AND_CHART_ALIAS)
                .setParameter("dashboardId", dashboardId)
                .setParameter("chartAlias", chartAlias));
    }
}
