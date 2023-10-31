package com.increff.omni.reporting.model.data.Charts;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class StackedBarChartData {
    private HashMap<String, List<String>> data;
}
