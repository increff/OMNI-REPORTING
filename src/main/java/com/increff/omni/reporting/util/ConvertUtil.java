package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.data.ChartLegendsData;
import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import lombok.extern.log4j.Log4j;

import java.util.List;

@Log4j
public class ConvertUtil {

    public static ChartLegendsData convertChartLegendsPojoToChartLegendsData(List<ChartLegendsPojo> pojos) {
        ChartLegendsData data = new ChartLegendsData();
        pojos.forEach(pojo -> {
            data.getLegends().put(pojo.getLegendKey(), pojo.getValue());
        });
        return data;
    }
}
