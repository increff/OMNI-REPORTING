package com.increff.omni.reporting.model.data.Charts;


import com.increff.omni.reporting.model.constants.ChartType;
import com.nextscm.commons.spring.common.ApiException;

import java.util.List;
import java.util.Map;

public class BarChartImpl implements ChartInterface {

    public Object transform(List<Map<String, String>> result) throws ApiException {
        System.out.println("BarData");
        BarChartData data = new BarChartData(); //todo check this and remove otherwise
//        data.setLabels(new ArrayList<>(result.get(0).keySet()));
//        data.setData(result.get(0).values().stream().mapToInt(Integer::parseInt).boxed().collect(Collectors.toList()));
//        data.setData(result.get(0));
        return data;
    }
}
