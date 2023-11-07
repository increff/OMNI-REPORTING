package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    REPORT(null,null, null), // TODO: RENAME TO TABLE ??

    BAR(1,null, null),
    GROUPED_BAR(null,null, null),
    STACKED_BAR(null,null, null),

    PIE(1,null, 100),
    DOUGHNUT(null,null, null),

    CARD(1,1, null),

    LINE(null,null, null),
    MULTI_LINE(null,null, null);


    private Integer ROW;
    private Integer COL;
    private Integer VALUE_SUM;

    ChartType(Integer row, Integer col, Integer valueSum) {
        this.ROW = row;
        this.COL = col;
        this.VALUE_SUM = valueSum;
    }
}
