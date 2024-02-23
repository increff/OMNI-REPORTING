package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ChartType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChartLayoutData {
    private Integer chartId;
    private String chartName;
    private ChartType type;
    private Integer colWidth;
}
