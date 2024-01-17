package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ChartLegendsDao;
import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ChartLegendsApi extends AbstractApi {

    @Autowired
    private ChartLegendsDao dao;

    public ChartLegendsPojo add(ChartLegendsPojo pojo) throws ApiException {
        dao.persist(pojo);
        return pojo;
    }

    public List<ChartLegendsPojo> put(Integer chartId, Map<String, String> map) throws ApiException {
        List<ChartLegendsPojo> pojos = new ArrayList<>();
        if(Objects.isNull(map) || map.isEmpty())return pojos;

        deleteByChartId(chartId); // Delete Existing Legends
        for (Map.Entry<String, String> entry : map.entrySet()) {
            ChartLegendsPojo pojo = new ChartLegendsPojo();
            pojo.setChartId(chartId);
            pojo.setLegendKey(entry.getKey());
            pojo.setValue(entry.getValue());
            add(pojo);
            pojos.add(pojo);
        }
        return pojos;
    }

    public List<ChartLegendsPojo> getByChartId(Integer chartId) {
        return dao.getByChartId(chartId);
    }

    public void deleteByChartId(Integer chartId) {
        List<ChartLegendsPojo> pojos = getByChartId(chartId);
        for(ChartLegendsPojo pojo : pojos){
            dao.remove(pojo);
        }
    }

}
