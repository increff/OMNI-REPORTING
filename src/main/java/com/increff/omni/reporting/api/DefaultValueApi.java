package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DefaultValueDao;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultValueApi extends AbstractApi {

    @Autowired
    private DefaultValueDao dao;

    public DefaultValuePojo upsert(DefaultValuePojo pojo) {
        DefaultValuePojo existing = getByDashboardAndControl(pojo.getDashboardId(), pojo.getControlId());
        if (Objects.nonNull(existing)) {
            existing.setDefaultValue(pojo.getDefaultValue());
            return existing;
        }
        dao.persist(pojo);
        return pojo;
    }

    public DefaultValuePojo getByDashboardAndControl(Integer dashboardId, Integer controlId)  {
        return dao.getByDashboardAndControl(dashboardId, controlId);
    }

    public void deleteByDashboardIdAndControlIdNotIn(Integer dashboardId, List<Integer> controlId) throws ApiException {
        dao.deleteByDashboardIdAndControlIdNotIn(dashboardId, controlId);
    }
}
