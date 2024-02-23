package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.RowHeight;
import com.increff.omni.reporting.model.form.*;

import java.util.List;

public class DashboardDtoTestHelper {

    public static DashboardAddForm getDashboardAddForm(String name, List<DashboardChartForm> charts) {
        DashboardAddForm form = new DashboardAddForm();
        form.setName(name);
        form.setCharts(charts);
        return form;
    }


    public static DashboardChartForm getDashboardChartForm(String chartAlias, Integer row, Integer col, Integer colWidth, RowHeight rowHeight) {
        DashboardChartForm form = new DashboardChartForm();
        form.setChartAlias(chartAlias);
        form.setRow(row);
        form.setCol(col);
        form.setColWidth(colWidth);
        form.setRowHeight(rowHeight);
        return form;
    }

    public static DashboardForm getDashboardForm(String name) {
        DashboardForm form = new DashboardForm();
        form.setName(name);
        return form;
    }

    public static DefaultValueForm getDefaultValueForm(Integer dashboardId, Integer controlId, List<String> defaultValue, String chartAlias) {
        DefaultValueForm form = new DefaultValueForm();
        form.setDashboardId(dashboardId);
        form.setControlId(controlId);
        form.setDefaultValue(defaultValue);
        form.setChartAlias(chartAlias);
        return form;
    }
}
