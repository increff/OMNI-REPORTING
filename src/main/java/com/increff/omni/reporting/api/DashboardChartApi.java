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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DashboardChartApi extends AbstractApi {

    @Autowired
    private DashboardChartDao dao;

    public DashboardChartPojo addDashboardChart(DashboardChartPojo pojo) throws ApiException{
        DashboardChartPojo existing = dao.getByDashboardAndChartAlias(pojo.getDashboardId(), pojo.getChartAlias());
        if(Objects.nonNull(existing))
            throw new ApiException(ApiStatus.BAD_DATA, "DashboardChart already exists with dashboardId: " + pojo.getDashboardId() + " chartAlias: " + pojo.getChartAlias());
        dao.persist(pojo);
        return pojo;
    }

    public List<DashboardChartPojo> getByDashboardIds(List<Integer> dashboardIds) {
        return dao.getByDashboardIds(dashboardIds).stream().distinct().collect(Collectors.toList());
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
