package com.increff.omni.reporting.model.data.Charts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MapListValuesChartData { // TODO: Rename to map multi value chart data
    private Map<String, List<String>> data;
}
