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
    public DefaultValuePojo getByDashboardControlChartAlias(Integer dashboardId, Integer controlId, String chartAlias) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_CONTROL_CHART_ALIAS)
                .setParameter("dashboardId", dashboardId)
                .setParameter("controlId", controlId)
                .setParameter("chartAlias", chartAlias));
    }

    private static final String DELETE_BY_DASHBOARD_CONTROL = "delete from DefaultValuePojo p where p.dashboardId=:dashboardId and p.controlId not in (:controlId)";
    public void deleteByDashboardIdAndControlIdNotIn(Integer dashboardId, List<Integer> controlIds) {
        if (controlIds.isEmpty()) return;
        createQuery(DELETE_BY_DASHBOARD_CONTROL)
                .setParameter("dashboardId", dashboardId)
                .setParameter("controlId", controlIds)
                .executeUpdate();
    }

}
