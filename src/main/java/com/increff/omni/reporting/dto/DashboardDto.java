package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.data.Charts.BarChartImpl;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.data.Charts.PieChartImpl;
import com.increff.omni.reporting.model.data.Charts.StackedBarChartImpl;
import com.increff.omni.reporting.model.form.DashboardForm;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.ConvertUtil.convertChartLegendsPojoToChartLegendsData;

@Service
@Log4j
@Setter
public class DashboardDto extends AbstractDto {
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DashboardApi api;
    @Autowired
    private DashboardChartApi dashboardChartApi;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private DefaultValueApi defaultValueApi;
    @Autowired
    private ChartLegendsApi chartLegendsApi;

    public DashboardData addDashboard(DashboardForm form) throws ApiException {
        checkValid(form);
        DashboardPojo dashboardPojo = ConvertUtil.convert(form, DashboardPojo.class);
        dashboardPojo.setOrgId(getOrgId());
        return getDashboard(api.add(dashboardPojo).getId());
    }

    public List<DashboardListData> getDashboardsByOrgId() {
        return getDashboardsByOrgId(getOrgId());
    }
    public List<DashboardListData> getDashboardsByOrgId(Integer orgId) {
        return ConvertUtil.convert(api.getByOrgId(orgId), DashboardListData.class);
    }

    public DashboardData getDashboard(Integer id) throws ApiException {
        DashboardPojo dashboard = api.getCheck(id, getOrgId());
        List<DashboardChartPojo> charts = dashboardChartApi.getByDashboardId(id);

        DashboardData dashboardData = new DashboardData();
        dashboardData.setDashboardDetails(ConvertUtil.convert(dashboard, DashboardForm.class));
        dashboardData.setFilterDetails(getFilterDetails(dashboard, charts));
        dashboardData.setGrid(getChartLayout(charts, getSchemaVersionId()));
        dashboardData.setId(dashboard.getId());

        return dashboardData;
    }

    public List<InputControlData> getFilterDetails(DashboardPojo dashboard, List<DashboardChartPojo> charts) throws ApiException {
        Integer orgSchemaVersionId = orgSchemaApi.getCheckByOrgId(dashboard.getOrgId()).getSchemaVersionId();
        HashMap<Integer, InputControlData> inputControlDataMap = new HashMap<>();
        for(DashboardChartPojo chart: charts){
            ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), orgSchemaVersionId, false);
            inputControlDto.selectForReport(report.getId()).forEach(inputControlData -> {
                inputControlDataMap.put(inputControlData.getId(), inputControlData);
            });
        }

        inputControlDataMap.values().forEach(inputControlData -> {
            inputControlData.setDefaultValue(null);
            DefaultValuePojo defaultValuePojo = defaultValueApi.getByDashboardAndControl(dashboard.getId(), inputControlData.getId());
            if(Objects.nonNull(defaultValuePojo))
                inputControlData.setDefaultValue(defaultValuePojo.getDefaultValue());
        });
        return new ArrayList<>(inputControlDataMap.values());
    }

    /**
     * @param charts 1D list of charts
     * @return 2D list of charts. Each row is a list of charts for that row in sorted order of col
     */
    private List<List<DashboardGridData>> getChartLayout(List<DashboardChartPojo> charts, Integer schemaVersionId) throws ApiException {
        // sort charts based on row and col
        charts = charts.stream().sorted((o1, o2) -> {
            if (o1.getRow() == o2.getRow()) {
                return o1.getCol() - o2.getCol();
            }
            return o1.getRow() - o2.getRow();
        }).collect(Collectors.toList());

        List<List<DashboardChartPojo>> chartList = new ArrayList<>();
        List<DashboardChartPojo> row = new ArrayList<>();
        Integer prevRow = null;
        for (DashboardChartPojo chart : charts) {
            if (prevRow == null) {
                prevRow = chart.getRow();
            }
            if (prevRow != chart.getRow()) {
                chartList.add(row);
                row = new ArrayList<>();
                prevRow = chart.getRow();
            }
            row.add(chart);
        }
        if(!row.isEmpty()) chartList.add(row);

        List<List<DashboardGridData>> chartData = new ArrayList<>();
        for (List<DashboardChartPojo> chartRow : chartList){
            List<DashboardGridData> chartRowData = ConvertUtil.convert(chartRow, DashboardGridData.class);
            for (DashboardGridData chart : chartRowData) {
                ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), schemaVersionId, false);
                chart.setName(report.getName());
                chart.setChartType(report.getChartType());
            }
            chartData.add(chartRowData);
        }
        return chartData;
    }

}
