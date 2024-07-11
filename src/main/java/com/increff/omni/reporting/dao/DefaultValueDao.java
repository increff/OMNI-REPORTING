package com.increff.omni.reporting.dao;

import com.increff.commons.springboot.db.dao.AbstractDao;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Repository
@Transactional
public class DefaultValueDao extends AbstractDao<DefaultValuePojo> {

    private static final String SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_AND_USER = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.paramName=:paramName and p.userId=:userId";
    private static final String SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_AND_USER_NULL = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.paramName=:paramName and p.userId is null";
    private static final String DELETE_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_NOT_IN = "delete from DefaultValuePojo p where p.dashboardId=:dashboardId and p.paramName not in (:paramName)";
    private static final String SELECT_BY_DASHBOARD_ID_USER_ID = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.userId=:userId";
    private static final String SELECT_BY_DASHBOARD_ID_USER_ID_NULL = "select p from DefaultValuePojo p where p.dashboardId=:dashboardId and p.userId is null";

    public DefaultValuePojo getByDashboardIdAndControlParamNameAndUser(Integer dashboardId, String paramName, Integer userId) {
        if (Objects.isNull(userId))
            return getByDashboardIdAndControlParamNameAndUserNull(dashboardId, paramName);

        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_AND_USER)
                .setParameter("dashboardId", dashboardId)
                .setParameter("paramName", paramName)
                .setParameter("userId", userId));
    }

    private DefaultValuePojo getByDashboardIdAndControlParamNameAndUserNull(Integer dashboardId, String paramName) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_DASHBOARD_ID_AND_CONTROL_PARAM_NAME_AND_USER_NULL)
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

    public List<DefaultValuePojo> getByDashboardIdUserId(Integer dashboardId, Integer userId) {
        if (Objects.isNull(userId))
            return getByDashboardIdUserIdNull(dashboardId);

        return selectMultiple(createJpqlQuery(SELECT_BY_DASHBOARD_ID_USER_ID)
                .setParameter("dashboardId", dashboardId)
                .setParameter("userId", userId));
    }

    private List<DefaultValuePojo> getByDashboardIdUserIdNull(Integer dashboardId) {
        return selectMultiple(createJpqlQuery(SELECT_BY_DASHBOARD_ID_USER_ID_NULL)
                .setParameter("dashboardId", dashboardId));
    }


}
