package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    REPORT(null,null, null),
    BAR(1,null, null),
    PIE(1,null, 100),
    SINGLE(1,1, null);

    private Integer ROW;
    private Integer COL;
    private Integer VALUE_SUM;

    ChartType(Integer row, Integer col, Integer valueSum) {
        this.ROW = row;
        this.COL = col;
        this.VALUE_SUM = valueSum;
    }
}
