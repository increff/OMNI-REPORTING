package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DashboardChartDao;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.nextscm.commons.spring.common.ApiException;
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

    //getByDashboardAndChartId
    public DashboardChartPojo getCheckByDashboardAndChartId(Integer dashboardId, Integer chartId) throws ApiException {
        DashboardChartPojo pojo = dao.getByDashboardAndChartId(dashboardId, chartId);
        checkNotNull(pojo, "DashboardChart does not exist dashboardId: " + dashboardId + " chartId: " + chartId);
        return pojo;
    }

    public void deleteByDashboardId(Integer id) throws ApiException {
        List<DashboardChartPojo> pojos = dao.getByDashboardId(id);
        for(DashboardChartPojo pojo : pojos){
            dao.remove(pojo);
        }
    }
}
