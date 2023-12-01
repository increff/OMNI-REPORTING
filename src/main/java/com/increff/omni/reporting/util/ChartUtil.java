package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.data.Charts.*;
import com.increff.omni.reporting.model.data.DashboardData;
import com.increff.omni.reporting.model.data.DashboardGridData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DashboardForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;

import java.util.*;

import static com.increff.omni.reporting.util.SqlCmd.getPieValueSum;

@Log4j
public class ChartUtil {

    private static final String PCT_VAL_COL = "percentageVal";

    public static MapSingleValueChartData getMapSingleValueChartData(List<Map<String, String>> result) {
        MapSingleValueChartData chartData = new MapSingleValueChartData();
        chartData.setData(result);
        return chartData;
    }

    public static MapSingleValueChartData getPieChartData(List<Map<String, String>> result) throws ApiException {
        // Normalize pie chart values so that their sum is 100
        MapSingleValueChartData chartData = new MapSingleValueChartData();

        result.forEach(row -> row.put(PCT_VAL_COL, "0"));
        List<String> columns = new ArrayList<>(result.get(0).keySet());
        String valueColumn = columns.get(1);
        // This contains a shallow copy of the maps!! Changes in the maps in this list will reflect in result list maps
        List<Map<String, String>> columnNameRowRemoved = result.subList(1, result.size());
        normalizeSumTo100(result, valueColumn, columnNameRowRemoved);
        chartData.setData(result);
        return chartData;
    }

    private static void normalizeSumTo100(List<Map<String, String>> result, String valueColumn, List<Map<String, String>> columnNameRowRemoved) throws ApiException {
        double sum = getPieValueSum(columnNameRowRemoved, valueColumn);
        if(sum != 0) {
            for (Map<String, String> map : columnNameRowRemoved) {
                map.put(PCT_VAL_COL, getNormalizedValue(map.get(valueColumn), sum));
            }

            double finalSum = getPieValueSum(columnNameRowRemoved, PCT_VAL_COL);
            double difference = 100 - finalSum;
            log.debug("difference: " + difference);
            if (difference != 0) { // As the final sum can be between(99.xx to 100.xx) due to precision, add the offset to first value
                columnNameRowRemoved.get(0).put(PCT_VAL_COL, String.format("%.2f", Double.parseDouble(columnNameRowRemoved.get(0).get(valueColumn)) + difference));
            }
            log.debug("result: " + result.size());
        }
    }


    public static MapMultiValueChartData getMapMultiValueChartData(List<Map<String, String>> result) {
        MapMultiValueChartData chart = new MapMultiValueChartData();
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
        chart.setData(data);
        return chart;
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

    public static String getNormalizedValue(String value, Double sum){
        log.debug("getNormalizedValue value: " + value + " sum: " + sum);
        return String.format("%.2f", (Double.parseDouble(value)/sum)*100); // returns rounding off to 2 decimals
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
