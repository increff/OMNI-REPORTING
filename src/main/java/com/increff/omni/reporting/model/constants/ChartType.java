package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    TABLE(null,null, null, null),
    // todo: set legends count to 2 for all bar/line charts before QA
    BAR(1,null, null, null),
    GROUPED_BAR(null,null, null, null),
    STACKED_BAR(null,null, null, null),

    PIE(1,null, 100, null),
    DOUGHNUT(null,null, null, null),

    CARD(1,1, null, null),

    LINE(null,null, null, null),
    MULTI_LINE(null,null, null, null);


    private Integer ROW_COUNT_VALIDATION; // No. of rows query output
    private Integer COL_COUNT_VALIDATION; // No. of cols query output
    private Integer VALUE_SUM_VALIDATION; // Sum of data for all rows (Eg. PIE CHART VALUES should sum to 100)
    private Integer LEGENDS_COUNT_VALIDATION; // No. of legends required for displaying this chart type

    ChartType(Integer row, Integer col, Integer valueSum, Integer legendsCount) {
        this.ROW_COUNT_VALIDATION = row;
        this.COL_COUNT_VALIDATION = col;
        this.VALUE_SUM_VALIDATION = valueSum;
        this.LEGENDS_COUNT_VALIDATION = legendsCount;
    }
}
