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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class ChartUtil {

    public static MapSingleValueChartData getGenericChartData(List<Map<String, String>> result) {
        MapSingleValueChartData chartData = new MapSingleValueChartData();
        chartData.setData(result.get(0));
        return chartData;
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

            case BAR:
            case LINE:

            case PIE:
            case DOUGHNUT:
                return new MapSingleValueChartDataImpl();

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
