package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DashboardChartDao;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardChartApi extends AbstractApi {

    @Autowired
    private DashboardChartDao dao;

    public DashboardChartPojo addDashboardChart(DashboardChartPojo pojo) {
        dao.persist(pojo);
        return pojo;
    }

    public List<DashboardChartPojo> getByDashboardId(Integer id) {
        return dao.getByDashboardId(id);
    }

    public List<DashboardChartPojo> getCheckByDashboardId(Integer id) throws ApiException {
        List<DashboardChartPojo> pojoList = getByDashboardId(id);
        if(pojoList.isEmpty()){
            throw new ApiException(ApiStatus.BAD_DATA, "No charts found for dashboardId: " + id);
        }
        return pojoList;
    }

    public DashboardChartPojo getCheckByDashboardAndChartAlias(Integer dashboardId, String chartAlias) throws ApiException {
        DashboardChartPojo pojo = dao.getByDashboardAndChartAlias(dashboardId, chartAlias);
        checkNotNull(pojo, "DashboardChart does not exist dashboardId: " + dashboardId + " chartAlias: " + chartAlias);
        return pojo;
    }

    public void deleteByDashboardId(Integer id) {
        List<DashboardChartPojo> pojos = dao.getByDashboardId(id);
        for(DashboardChartPojo pojo : pojos){
            dao.remove(pojo);
        }
    }
}
