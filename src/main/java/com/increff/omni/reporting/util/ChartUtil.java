package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.data.Charts.MapMultiValueChartData;
import com.increff.omni.reporting.model.data.Charts.MapSingleValueChartData;
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
}
