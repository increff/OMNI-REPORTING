package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class DefaultValueDao extends AbstractDao<DefaultValuePojo> {

    private static final String SELECT_BY_DASHBOARD_CONTROL_CHART_ALIAS = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.controlId=:controlId and p.chartAlias=:chartAlias";
    private static final String SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.paramName=:paramName";
    private static final String DELETE_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_NOT_IN = "delete from DefaultValuePojo p where p.dashboardId=:dashboardId and p.paramName not in (:paramName)";
    private static final String DELETE_BY_DASHBOARD_CONTROL_CHART_ALIAS_NOT_IN = "delete from DefaultValuePojo p where p.dashboardId=:dashboardId and p.controlId=:controlId and p.chartAlias not in (:chartAlias)";


    public DefaultValuePojo getByDashboardControlChartAlias(Integer dashboardId, Integer controlId, String chartAlias) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_CONTROL_CHART_ALIAS)
                .setParameter("dashboardId", dashboardId)
                .setParameter("controlId", controlId)
                .setParameter("chartAlias", chartAlias));
    }

    public DefaultValuePojo getByDashboardIdAndControlParamName(Integer dashboardId, String paramName) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME)
                .setParameter("dashboardId", dashboardId)
                .setParameter("paramName", paramName));
    }

    public void deleteByDashboardIdAndControlParamNameNotIn(Integer dashboardId, List<String> paramName) {
        if (paramName.isEmpty()) return;
        createQuery(DELETE_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_NOT_IN)
                .setParameter("dashboardId", dashboardId)
                .setParameter("paramName", paramName)
                .executeUpdate();
    }

    public void deleteByDashboardControlChartAliasNotIn(Integer dashboardId, Integer controlId, List<String> chartAlias) {
        if (chartAlias.isEmpty()) return;
        createQuery(DELETE_BY_DASHBOARD_CONTROL_CHART_ALIAS_NOT_IN)
                .setParameter("dashboardId", dashboardId)
                .setParameter("controlId", controlId)
                .setParameter("chartAlias", chartAlias)
                .executeUpdate();
    }

}
