package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DefaultValueDao;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.server.AbstractApi;
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
        DefaultValuePojo existing = getByDashboardControlChartAlias(pojo.getDashboardId(), pojo.getControlId(), pojo.getChartAlias());
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

    public DefaultValuePojo getByDashboardControlChartAlias(Integer dashboardId, Integer controlId, String chartAlias) {
        return dao.getByDashboardControlChartAlias(dashboardId, controlId, chartAlias);
    }

    public List<DefaultValuePojo> getByDashboardId(Integer dashboardId) {
        return dao.selectMultiple("dashboardId", dashboardId);

    }

    public void deleteByDashboardId(Integer dashboardId) {
        List<DefaultValuePojo> pojos = dao.selectMultiple("dashboardId", dashboardId);
        for (DefaultValuePojo pojo : pojos) {
            dao.remove(pojo);
        }
    }

    public void deleteByDashboardIdAndControlIdNotIn(Integer dashboardId, List<Integer> controlId) throws ApiException {
        dao.deleteByDashboardIdAndControlIdNotIn(dashboardId, controlId);
    }

    public void deleteByDashboardControlChartAlias(Integer dashboardId, Integer controlId, String chartAlias) {
        DefaultValuePojo pojo = getByDashboardControlChartAlias(dashboardId, controlId, chartAlias);
        if (Objects.nonNull(pojo)) {
            dao.remove(pojo);
        }
    }

    public void deleteByDashboardControlChartAliasNotIn(Integer dashboardId, Integer controlId, List<String> chartAlias) {
        dao.deleteByDashboardControlChartAliasNotIn(dashboardId, controlId, chartAlias);
    }
}
