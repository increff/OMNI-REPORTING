package com.increff.omni.reporting.api;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.server.AbstractApi;
import com.increff.omni.reporting.dao.DefaultValueDao;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DefaultValueApi extends AbstractApi {

    @Autowired
    private DefaultValueDao dao;

    public DefaultValuePojo upsert(DefaultValuePojo pojo) {
        DefaultValuePojo existing = getByDashboardIdAndControlParamNameAndUser(pojo.getDashboardId(), pojo.getParamName(), pojo.getUserId());
        if (Objects.nonNull(existing)) {
            if(pojo.getDefaultValue().isEmpty()){
                dao.remove(existing);
                return new DefaultValuePojo();
            }
            existing.setDefaultValue(pojo.getDefaultValue());
            return existing;
        }
        if(pojo.getDefaultValue().isEmpty())
            return new DefaultValuePojo(); // If default value is empty, don't add it
        dao.persist(pojo);
        return pojo;
    }

    public DefaultValuePojo getByDashboardIdAndControlParamNameAndUser(Integer dashboardId, String controlParamName, Integer userId) {
        return dao.getByDashboardIdAndControlParamNameAndUser(dashboardId, controlParamName, userId);
    }

    public List<DefaultValuePojo> getByDashboardIdUserId(Integer dashboardId, Integer userId) {
        return dao.getByDashboardIdUserId(dashboardId, userId);
    }

    public void deleteByDashboardId(Integer dashboardId) {
        List<DefaultValuePojo> pojos = dao.selectMultiple("dashboardId", dashboardId);
        for (DefaultValuePojo pojo : pojos) {
            dao.remove(pojo);
        }
    }

    public void deleteByDashboardIdUserId(Integer dashboardId, Integer userId) {
        List<DefaultValuePojo> pojos = getByDashboardIdUserId(dashboardId, userId);
        for (DefaultValuePojo pojo : pojos) {
            dao.remove(pojo);
        }
    }

    public void deleteByDashboardIdAndControlParamNameNotIn(Integer dashboardId, List<String> paramName) throws ApiException {
        dao.deleteByDashboardIdAndControlParamNameNotIn(dashboardId, paramName);
    }

}
