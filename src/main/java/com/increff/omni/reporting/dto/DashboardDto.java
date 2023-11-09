package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.data.Charts.MapSingleValueChartDataImpl;
import com.increff.omni.reporting.model.data.Charts.MapMultiValuesChartDataImpl;
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

    public Map<String,List<InputControlData>> getFilterDetails(DashboardPojo dashboard, List<DashboardChartPojo> charts) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = new HashMap<>();
        Integer orgSchemaVersionId = orgSchemaApi.getCheckByOrgId(dashboard.getOrgId()).getSchemaVersionId();
        Map<Integer, String> controlDefaultValueMap = defaultValueApi.getByDashboardId(dashboard.getId())
                .stream().collect(Collectors.toMap(DefaultValuePojo::getControlId, DefaultValuePojo::getDefaultValue));

        for(DashboardChartPojo chart: charts){
            ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), orgSchemaVersionId, true);
            filterDetails.put(report.getAlias(), new ArrayList<>());
            inputControlDto.selectForReport(report.getId()).forEach(inputControlData -> {
                inputControlData.setDefaultValue(controlDefaultValueMap.getOrDefault(inputControlData.getId(), null));
                filterDetails.get(report.getAlias()).add(inputControlData);
            });
        }

        extractCommonFilters(charts, filterDetails);

        return filterDetails;
    }

    private void extractCommonFilters(List<DashboardChartPojo> charts, Map<String, List<InputControlData>> filterDetails) {
        List<InputControlData> commonFilters = new ArrayList<>();
        for(InputControlData filter: filterDetails.get(charts.get(0).getChartAlias())){
            boolean isCommon = true;
            for(DashboardChartPojo chart: charts){
                if(filterDetails.get(chart.getChartAlias()).stream().noneMatch(filter1 -> filter1.getId().equals(filter.getId()))){
                    isCommon = false; // if filter is not present in a chart, it is not common
                    break;
                }
            }
            if(isCommon){
                commonFilters.add(filter);
                for(DashboardChartPojo chart: charts){ // remove common filter from chart level filters
                    filterDetails.get(chart.getChartAlias()).removeIf(filter1 -> filter1.getId().equals(filter.getId()));
                }
            }
        }
        filterDetails.put("common", commonFilters);
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
                ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), schemaVersionId, true);
                chart.setName(report.getName());
                chart.setChartType(report.getChartType());
                chart.setChartId(report.getId());
            }
            chartData.add(chartRowData);
        }
        return chartData;
    }

    public List<ViewDashboardData> viewDashboard(ReportRequestForm form, Integer dashboardId) throws ApiException, IOException {
        DashboardPojo dashboard = api.getCheck(dashboardId, getOrgId());
        ReportPojo report = reportApi.getCheck(form.getReportId());
        DashboardChartPojo charts = dashboardChartApi.getCheckByDashboardAndChartAlias(dashboardId, report.getAlias());
        ChartType type = report.getChartType();

        List<Map<String, String>> data = reportDto.getLiveData(form);

        ChartInterface chartInterface = getChartData(type);
        chartInterface.validate(data, type);

        ViewDashboardData viewData = getViewDashboardData(report, charts, type, data, chartInterface);
        return Collections.singletonList(viewData);
    }

    private ViewDashboardData getViewDashboardData(ReportPojo report, DashboardChartPojo charts, ChartType type, List<Map<String, String>> data, ChartInterface chartInterface) throws ApiException {
        ViewDashboardData viewData = new ViewDashboardData();
        viewData.setChartData(chartInterface.transform(data));
        viewData.setLegends(convertChartLegendsPojoToChartLegendsData(
                chartLegendsApi.getByChartId(report.getId())).getLegends());
        viewData.setChartId(report.getId());
        viewData.setType(type);
        viewData.setRow(charts.getRow());
        viewData.setCol(charts.getCol());
        viewData.setColWidth(charts.getColWidth());
        return viewData;
    }

    private ChartInterface getChartData(ChartType type) throws ApiException {
        switch (type) {
            case REPORT:
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

}
