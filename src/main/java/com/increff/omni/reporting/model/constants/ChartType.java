package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    REPORT(null,null), BAR(1,null), PIE(1,null), SINGLE(1,1);

    private Integer ROW;
    private Integer COL;
    // TODO: Add value sum validation for PIE

    ChartType(Integer row, Integer col) {
        this.ROW = row;
        this.COL = col;
    }
}
