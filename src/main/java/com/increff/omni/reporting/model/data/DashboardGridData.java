package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DashboardGridData extends DashboardChartForm {
    private ChartType chartType;
    private String name;
    private Integer chartId;
}
