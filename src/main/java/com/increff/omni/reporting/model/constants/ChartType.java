package com.increff.omni.reporting.model.constants;

import lombok.Getter;

// ToDo: add validations for all types of charts and their return values
@Getter
public enum ChartType {
    TABLE(null,null, null, null),

    BAR(1,null, null, null),
    GROUPED_BAR(null,null, null, null),
    STACKED_BAR(null,null, null, null),

    PIE(1,null, 100, null),
    DOUGHNUT(null,null, null, null),

    CARD(1,1, null, null),

    LINE(null,null, null, null),
    MULTI_LINE(null,null, null, null);


    private Integer ROW_COUNT_VALIDATION;
    private Integer COL_COUNT_VALIDATION;
    private Integer VALUE_SUM_VALIDATION;
    private Integer LEGENDS_COUNT_VALIDATION;

    ChartType(Integer row, Integer col, Integer valueSum, Integer legendsCount) {
        this.ROW_COUNT_VALIDATION = row;
        this.COL_COUNT_VALIDATION = col;
        this.VALUE_SUM_VALIDATION = valueSum;
        this.LEGENDS_COUNT_VALIDATION = legendsCount;
    }
}
