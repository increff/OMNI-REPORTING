package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    TABLE(null,null, null, null),
    CARD(1,null, null, null),

    BAR(null,2, null, 2),
    GROUPED_BAR(null,null, null, 2),
    STACKED_BAR(null,null, null, 2),

    PIE(1,null, null, null),
    DOUGHNUT(null,null, null, null),

    LINE(2,null, null, 2),
    MULTI_LINE(null,null, null, 2);


    private final Integer ROW_COUNT_VALIDATION; // No. of rows query output
    private final Integer COL_COUNT_VALIDATION; // No. of cols query output
    private final Integer VALUE_SUM_VALIDATION; // Sum of data for all rows (Eg. PIE CHART VALUES should sum to 100)
    private final Integer LEGENDS_COUNT_VALIDATION; // No. of legends required for creating this chart type

    ChartType(Integer row, Integer col, Integer valueSum, Integer legendsCount) {
        this.ROW_COUNT_VALIDATION = row;
        this.COL_COUNT_VALIDATION = col;
        this.VALUE_SUM_VALIDATION = valueSum;
        this.LEGENDS_COUNT_VALIDATION = legendsCount;
    }
}
