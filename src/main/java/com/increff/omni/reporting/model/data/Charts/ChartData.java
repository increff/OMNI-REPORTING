package com.increff.omni.reporting.model.data.Charts;


import java.util.List;
import java.util.Map;

public interface ChartData {
    public Object transform(List<Map<String, String>> data);
}
