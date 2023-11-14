package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ChartType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ViewDashboardData {
    private Integer chartId;
    private ChartType type;
    private Integer row;
    private Integer col;
    private Integer colWidth;
    private Object chartData;//todo check what is being done in UI for handling java object
    private Map<String, String> legends;
}
