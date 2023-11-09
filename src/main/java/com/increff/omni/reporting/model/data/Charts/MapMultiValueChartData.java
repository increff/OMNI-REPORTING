package com.increff.omni.reporting.model.data.Charts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MapMultiValueChartData {
    private Map<String, List<String>> data;
}
