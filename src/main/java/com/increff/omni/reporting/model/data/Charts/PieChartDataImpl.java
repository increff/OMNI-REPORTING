package com.increff.omni.reporting.model.data.Charts;


import com.nextscm.commons.spring.common.ApiException;

import java.util.List;
import java.util.Map;

import static com.increff.omni.reporting.util.ChartUtil.getPieChartData;

public class PieChartDataImpl implements ChartInterface {
    public Object transform(List<Map<String, String>> result) throws ApiException {
        return getPieChartData(result);
    }
}
