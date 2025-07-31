package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum ChartType {
    REPORT(null, null, null, null, false), // Used for normal reports

    TABLE(null,null, null, null, false),
    CARD(1,null, null, null, false),

    BAR(null,2, null, 2, false),
    GROUPED_BAR(null,null, null, 2, false),
    STACKED_BAR(null,null, null, 2, false),

    PIE(null,2, null, null, false),
    DOUGHNUT(null,2, null, null, false),

    LINE(null,null, null, 2, true),
    MULTI_LINE(null,null, null, 2, true);


    private final Integer ROW_COUNT_VALIDATION; // No. of rows query output
    private final Integer COL_COUNT_VALIDATION; // No. of cols query output
    private final Integer VALUE_SUM_VALIDATION; // Sum of data for all rows (Eg. PIE CHART VALUES should sum to 100)
    private final Integer LEGENDS_COUNT_VALIDATION; // No. of legends required for creating this chart type
    private final Boolean CAN_BENCHMARK; // Whether this chart type can be benchmarked
    ChartType(Integer row, Integer col, Integer valueSum, Integer legendsCount, Boolean canBenchmark) {
        this.ROW_COUNT_VALIDATION = row;
        this.COL_COUNT_VALIDATION = col;
        this.VALUE_SUM_VALIDATION = valueSum;
        this.LEGENDS_COUNT_VALIDATION = legendsCount;
        this.CAN_BENCHMARK = canBenchmark;
    }
}
