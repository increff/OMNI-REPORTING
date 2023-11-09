package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.data.Charts.GenericChartData;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;

@Log4j
public class ChartUtil {

    public static GenericChartData getGenericChartData(List<Map<String, String>> result) {
        GenericChartData chartData = new GenericChartData();
        chartData.setData(result.get(0));
        return chartData;
    }
}
