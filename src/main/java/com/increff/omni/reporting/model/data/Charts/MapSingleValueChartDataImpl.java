package com.increff.omni.reporting.model.data.Charts;


import java.util.List;
import java.util.Map;

import static com.increff.omni.reporting.util.ChartUtil.getMapSingleValueChartData;

public class MapSingleValueChartDataImpl implements ChartInterface {
    public Object transform(List<Map<String, String>> result){
        return getMapSingleValueChartData(result);
    }
}
