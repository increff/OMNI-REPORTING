package com.increff.omni.reporting.model.data.Charts;

import java.util.List;
import java.util.Map;

import static com.increff.omni.reporting.util.ChartUtil.getMapMultiValueChartData;

public class MapMultiValuesChartDataImpl implements ChartInterface {

    public Object transform(List<Map<String, String>> result){
        return getMapMultiValueChartData(result);
    }

}
