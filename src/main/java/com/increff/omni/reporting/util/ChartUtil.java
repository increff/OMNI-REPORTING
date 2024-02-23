package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.data.Charts.*;
import com.increff.omni.reporting.model.data.DashboardData;
import com.increff.omni.reporting.model.data.DashboardGridData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DashboardForm;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;

import java.util.*;

import static com.increff.omni.reporting.util.SqlCmd.getValueSum;

@Log4j2
public class ChartUtil {

    public static final String DEFAULT_VALUE_COMMON_KEY = "common";
    private static final String PCT_VAL_COL = "percentageVal";

    public static MapSingleValueChartData getMapSingleValueChartData(List<Map<String, String>> result) {
        if (result.isEmpty()) return null;
        MapSingleValueChartData chartData = new MapSingleValueChartData();
        chartData.setData(result);
        return chartData;
    }

    public static MapSingleValueChartData getPieChartData(List<Map<String, String>> result) throws ApiException {
        if(result.isEmpty()) return null;
        MapSingleValueChartData chartData = new MapSingleValueChartData();

        result.forEach(row -> row.put(PCT_VAL_COL, "0")); // initialize the percentage column to 0
        List<String> columns = new ArrayList<>(result.get(0).keySet());
        String valueColumn = columns.get(1);

        normalizeSumTo100(valueColumn, result);

        chartData.setData(result);
        return chartData;
    }

    private static void normalizeSumTo100(String valueColumn, List<Map<String, String>> columnNameRowRemoved) throws ApiException {
        double sum = getValueSum(columnNameRowRemoved, valueColumn);
        if(sum != 0) {
            for (Map<String, String> map : columnNameRowRemoved) {
                map.put(PCT_VAL_COL, getNormalizedValue(map.get(valueColumn), sum));
            }

            double finalSum = getValueSum(columnNameRowRemoved, PCT_VAL_COL);
            double difference = 100 - finalSum;
            log.debug("difference: " + difference);
            if (difference != 0) { // As the final sum can be between(99.xx to 100.xx) due to precision, add the offset to first value
                columnNameRowRemoved.get(0).put(PCT_VAL_COL, String.format("%.2f", Double.parseDouble(columnNameRowRemoved.get(0).get(PCT_VAL_COL)) + difference));
            } // TODO: Make pct sum 100 by calculating final row pct after  from sum of the rest instead of doing above BS
        }
    }

    public static String getNormalizedValue(String value, Double sum){
        log.debug("getNormalizedValue value: " + value + " sum: " + sum);
        return String.format("%.2f", (Double.parseDouble(value)/sum)*100); // returns rounding off to 2 decimals
    }

    public static MapMultiValueChartData getMapMultiValueChartData(List<Map<String, String>> result) {
        if(result.isEmpty()) return null;
        MapMultiValueChartData chartData = new MapMultiValueChartData();
        if (result.isEmpty()) return chartData;
        // first column values is treated as labels
        // each column is a dataset
        // each row has 1 value for each dataset and the label corresponding to that value
        Map<String, List<String>> data = new HashMap<>();
        List<String> columns = new ArrayList<>(result.get(0).keySet());
        String labelColumn = columns.get(0);
        List<String> valueColumns = columns.subList(1, columns.size());

        data.put("labels", new ArrayList<>());
        for (String valueColumn : valueColumns) {
            data.put(valueColumn, new ArrayList<>());
        }

        for (Map<String, String> row : result) {
            data.get("labels").add(row.get(labelColumn));
            for (String valueColumn : valueColumns) {
                data.get(valueColumn).add(row.get(valueColumn));
            }
        }
        chartData.setData(data);
        return chartData;
    }


    public static ChartInterface getChartData(ChartType type) throws ApiException {
        switch (type) {
            case TABLE:
            case CARD:
                return new MapSingleValueChartDataImpl();
            case PIE:
            case DOUGHNUT:
                return new PieChartDataImpl();

            case BAR:
            case LINE:

            case GROUPED_BAR:
            case STACKED_BAR:
            case MULTI_LINE:
                return new MapMultiValuesChartDataImpl();

            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Chart Data Implementation not found for type: " + type);
        }
    }

    public static DashboardData getDashboardData(Integer dashboardId, DashboardForm form, Map<String, List<InputControlData>> filters, List<List<DashboardGridData>> grid) {
        DashboardData dashboardData = new DashboardData();
        dashboardData.setDashboardDetails(form);
        dashboardData.setFilterDetails(filters);
        dashboardData.setGrid(grid);
        dashboardData.setId(dashboardId);
        return dashboardData;
    }
}
