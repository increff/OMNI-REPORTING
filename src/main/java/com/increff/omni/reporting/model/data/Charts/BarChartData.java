package com.increff.omni.reporting.model.data.Charts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BarChartData {
    List<String> labels;
    List<Integer> data;
}
