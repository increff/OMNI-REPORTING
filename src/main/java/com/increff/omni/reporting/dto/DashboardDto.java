package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.form.DashboardAddForm;
import com.increff.omni.reporting.model.form.DashboardForm;
import com.increff.omni.reporting.model.form.DefaultValueForm;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.ChartUtil;
import com.increff.omni.reporting.util.ValidateUtil;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.ChartUtil.DEFAULT_VALUE_COMMON_KEY;
import static com.increff.omni.reporting.util.ChartUtil.getChartData;
import static com.increff.omni.reporting.util.ConvertUtil.convertChartLegendsPojoToChartLegendsData;

@Service
@Log4j
@Setter
public class DashboardDto extends AbstractDto {
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private DashboardApi api;
    @Autowired
    private DashboardChartApi dashboardChartApi;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private DefaultValueApi defaultValueApi;
    @Autowired
    private ChartLegendsApi chartLegendsApi;
    @Autowired
    private DashboardChartDto dashboardChartDto;
    @Autowired
    private ApplicationProperties properties;

    @Transactional(rollbackFor = ApiException.class)
    public ApplicationPropertiesData getProperties() {
        ApplicationPropertiesData data = new ApplicationPropertiesData();
        data.setMaxDashboardsPerOrg(properties.getMaxDashboardsPerOrg());
        data.setMaxChartsPerDashboard(ValidateUtil.MAX_DASHBOARD_CHARTS);
        return data;
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<DefaultValueData> upsertDefaultValues(List<DefaultValueForm> forms) throws ApiException {
        List<DefaultValuePojo> pojos = new ArrayList<>();

        defaultValueApi.deleteByDashboardId(forms.get(0).getDashboardId()); // Delete all existing default values for dashboard

        for(DefaultValueForm form : forms) {
            checkValid(form);
            api.getCheck(form.getDashboardId(), getOrgId());
            validateControlIdExistsForDashboard(form.getDashboardId(), form.getControlId());

            DefaultValuePojo pojo = ConvertUtil.convert(form, DefaultValuePojo.class);
            pojo.setDefaultValue(String.join(",", form.getDefaultValue()));
            pojos.add(defaultValueApi.upsert(pojo));
        }
        return ConvertUtil.convert(pojos, DefaultValueData.class);
    }



    @Transactional(rollbackFor = ApiException.class)
    public DashboardData addDashboard(DashboardAddForm form) throws ApiException {
        validateDashboardAddForm(form);

        DashboardPojo dashboardPojo = ConvertUtil.convert(form, DashboardPojo.class);

        dashboardPojo.setOrgId(getOrgId());
        api.add(dashboardPojo);
        dashboardChartDto.addDashboardChart(form.getCharts(), dashboardPojo.getId());

        return getDashboard(dashboardPojo.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public DashboardData updateDashboard(DashboardForm form, Integer dashboardId) throws ApiException {
        validateDashboardForm(form);
        api.getCheck(dashboardId, getOrgId());
        DashboardPojo dashboard = api.update(dashboardId, ConvertUtil.convert(form, DashboardPojo.class));
        return getDashboard(dashboard.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public void deleteDashboard(Integer dashboardId) throws ApiException {
        dashboardChartApi.deleteByDashboardId(dashboardId);
        defaultValueApi.deleteByDashboardId(dashboardId);
        api.delete(dashboardId);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<DashboardListData> getDashboardsByOrgId() throws ApiException {
        return getDashboardsByOrgId(getOrgId());
    }

    public List<DashboardListData> getDashboardsByOrgId(Integer orgId) throws ApiException {
        List<DashboardListData> data = ConvertUtil.convert(api.getByOrgId(orgId), DashboardListData.class);

        if (isCustomReportUser()) { // Filter out standard dashboards for custom users
            List<Integer> customDashboardIds = getCustomDashboardIds(orgSchemaApi.getCheckByOrgId(orgId).getSchemaVersionId(),
                    data.stream().map(DashboardListData::getId).collect(Collectors.toList()));
            data = data.stream().filter(dashboard -> customDashboardIds.contains(dashboard.getId())).collect(Collectors.toList());
        }
        data.sort(Comparator.comparing(DashboardListData::getName));
        return data;
    }

    @Transactional(rollbackFor = ApiException.class)
    public DashboardData getDashboard(Integer id) throws ApiException {
        DashboardPojo dashboard = api.getCheck(id, getOrgId());
        List<DashboardChartPojo> charts = dashboardChartApi.getCheckByDashboardId(id);

        return ChartUtil.getDashboardData(dashboard.getId(), ConvertUtil.convert(dashboard, DashboardForm.class),
                getFilterDetails(dashboard, charts), getChartLayout(charts, getSchemaVersionId()));
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String,List<InputControlData>> getFilterDetails(DashboardPojo dashboard, List<DashboardChartPojo> charts) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = new HashMap<>();

        setFilterDefaults(dashboard, charts, filterDetails);

        extractCommonFilters(charts, filterDetails);

        return filterDetails;
    }

    private void setFilterDefaults(DashboardPojo dashboard, List<DashboardChartPojo> charts, Map<String, List<InputControlData>> filterDetails) throws ApiException {
        Integer orgSchemaVersionId = orgSchemaApi.getCheckByOrgId(dashboard.getOrgId()).getSchemaVersionId();
        Map<String, Map<Integer, String>> chartDefaultValueMap = defaultValueApi.getByDashboardId(dashboard.getId())
                .stream().collect(Collectors.groupingBy(DefaultValuePojo::getChartAlias,
                        Collectors.toMap(DefaultValuePojo::getControlId, DefaultValuePojo::getDefaultValue)));

        for(DashboardChartPojo chart: charts){
            Map<Integer, String> controlDefaultValueMap = chartDefaultValueMap.getOrDefault(chart.getChartAlias(), new HashMap<>());
            controlDefaultValueMap.putAll(chartDefaultValueMap.getOrDefault(DEFAULT_VALUE_COMMON_KEY, new HashMap<>()));

            ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), orgSchemaVersionId, true);

            filterDetails.put(report.getAlias(), new ArrayList<>());
            inputControlDto.selectForReport(report.getId()).forEach(inputControlData -> {
                inputControlData.setDefaultValue(controlDefaultValueMap.getOrDefault(inputControlData.getId(), null));
                if(Objects.nonNull(inputControlData.getDefaultValue()) && Objects.nonNull(inputControlData.getQuery()))
                    inputControlData.setDefaultValue(String.join(",", getAllowedValuesInDefaults(controlDefaultValueMap, inputControlData)));

                filterDetails.get(report.getAlias()).add(inputControlData);
            });
        }
    }

    private List<String> getAllowedValuesInDefaults(Map<Integer, String> controlDefaultValueMap, InputControlData inputControlData) {
        List<String> allowedValues = inputControlData.getOptions().stream().map(InputControlData.InputControlDataValue::getLabelName).collect(Collectors.toList());
        List<String> defaults = getValuesFromList(controlDefaultValueMap.get(inputControlData.getId()));
        defaults = defaults.stream().filter(allowedValues::contains).collect(Collectors.toList());
        return defaults;
    }

    private List<String> getValuesFromList(String values) {
        if (StringUtil.isEmpty(values))
            return new ArrayList<>();
        return Arrays.asList(values.split(","));
    }

    private void extractCommonFilters(List<DashboardChartPojo> charts, Map<String, List<InputControlData>> filterDetails) {
        List<InputControlData> commonFilters = new ArrayList<>();
        for (InputControlData filter : filterDetails.get(charts.get(0).getChartAlias())) {
            boolean isCommon = true;
            for (DashboardChartPojo chart : charts) {
                if (filterDetails.get(chart.getChartAlias()).stream().noneMatch(filter1 -> filter1.getId().equals(filter.getId()))) {
                    isCommon = false; // if filter is not present in a chart, it is not common
                    break;
                }
            }
            if (isCommon) {
                commonFilters.add(filter);
            }
        }
        filterDetails.put("common", commonFilters);

        for (InputControlData filter : commonFilters){
            for (DashboardChartPojo chart : charts) { // remove common filter from chart level filters
                filterDetails.get(chart.getChartAlias()).removeIf(filter1 -> filter1.getId().equals(filter.getId()));
            }
        }
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

    @Transactional(rollbackFor = ApiException.class)
    public List<ViewDashboardData> viewDashboard(ReportRequestForm form, Integer dashboardId) throws ApiException, IOException {
        DashboardPojo dashboard = api.getCheck(dashboardId, getOrgId());
        ReportPojo report = reportApi.getCheck(form.getReportId());
        if(!report.getIsEnabled())
            throw new ApiException(ApiStatus.BAD_DATA, "Chart disabled: " + report.getName());
        DashboardChartPojo charts = dashboardChartApi.getCheckByDashboardAndChartAlias(dashboardId, report.getAlias());
        ChartType type = report.getChartType();

        List<Map<String, String>> data = reportDto.getLiveData(form);

        ChartInterface chartInterface = getChartData(type);
        chartInterface.validateNormalize(data, type);

        ViewDashboardData viewData = getViewDashboardData(report, charts, data, chartInterface);
        return Collections.singletonList(viewData);
    }

    private ViewDashboardData getViewDashboardData(ReportPojo report, DashboardChartPojo charts,
                                                   List<Map<String, String>> data, ChartInterface chartInterface) throws ApiException {
        ViewDashboardData viewData = new ViewDashboardData();
        viewData.setChartData(chartInterface.transform(data));
        viewData.setLegends(convertChartLegendsPojoToChartLegendsData(chartLegendsApi.getByChartId(report.getId()))
                .getLegends());
        viewData.setChartId(report.getId());
        viewData.setType(report.getChartType());
        viewData.setRow(charts.getRow());
        viewData.setCol(charts.getCol());
        viewData.setColWidth(charts.getColWidth());
        return viewData;
    }

    private void validateDashboardForm(DashboardForm form) throws ApiException {
        checkValid(form);
        if(Objects.nonNull(api.getByOrgIdName(getOrgId(), form.getName())))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard name already exists: " + form.getName() + " OrgId: " + getOrgId());
    }

    private void validateDashboardAddForm(DashboardAddForm form) throws ApiException {
        ValidateUtil.validateDashboardAddForm(form);
        if(Objects.nonNull(api.getByOrgIdName(getOrgId(), form.getName())))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard name already exists: " + form.getName() + " OrgId: " + getOrgId());
        if(api.getByOrgId(getOrgId()).size() >= properties.getMaxDashboardsPerOrg())
            throw new ApiException(ApiStatus.BAD_DATA, "Max limit of dashboards reached: " + properties.getMaxDashboardsPerOrg());
    }


    private void validateControlIdExistsForDashboard(Integer dashboardId, Integer controlId) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = getFilterDetails(api.getCheck(dashboardId, getOrgId()),
                dashboardChartApi.getByDashboardId(dashboardId));
        if(filterDetails.values().stream().flatMap(List::stream).noneMatch(inputControlData -> inputControlData.getId().equals(controlId))){
            throw new ApiException(ApiStatus.BAD_DATA, "Control Id does not exist for dashboard id: " + dashboardId + " control id: " + controlId);
        }
    }


    @Transactional(rollbackFor = ApiException.class)
    public void copyDashboardToAllOrgs(Integer dbIdToCopy, Integer orgIdToCopy) throws ApiException {
        // NOTE : This copies charts only! NOT default values!
    	DashboardPojo oldDb = api.getCheck(dbIdToCopy, orgIdToCopy);
        Set<Integer> orgIds = organizationApi.getAll().stream().map(OrganizationPojo::getId).collect(Collectors.toSet());
        orgIds.remove(orgIdToCopy);

        for(Integer orgId : orgIds) {
            DashboardPojo newDbPojo = duplicateDashboard(oldDb, orgId);
            duplicateDashboardCharts(dbIdToCopy, newDbPojo.getId());
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public void copyDashboardToSomeOrgs(Integer dbIdToCopy, Integer orgIdToCopy, List<Integer> orgIds) throws ApiException {
        // NOTE : This copies charts only! NOT default values!
    	DashboardPojo oldDb = api.getCheck(dbIdToCopy, orgIdToCopy);
        for(Integer orgId : orgIds) {
            DashboardPojo newDbPojo = duplicateDashboard(oldDb, orgId);
            duplicateDashboardCharts(dbIdToCopy, newDbPojo.getId());
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public void copyDashboardToNewOrgs(List<Integer> orgIds) throws ApiException {
        // Copies all dashboards created in Increff org (Admin org set in properties file) to new orgs
        List<DashboardPojo> dashboards = api.getByOrgId(properties.getIncreffOrgId());
        for(DashboardPojo dashboard : dashboards)
            copyDashboardToSomeOrgs(dashboard.getId(), dashboard.getOrgId(), orgIds);
    }

    @Transactional(rollbackFor = ApiException.class)
    private void duplicateDashboardCharts(Integer oldDbId, Integer newDbId) throws ApiException{
        List<DashboardChartPojo> charts = dashboardChartApi.getByDashboardId(oldDbId);
        for(DashboardChartPojo chart : charts) {
            DashboardChartPojo dashboardChartPojo = ConvertUtil.convert(chart, DashboardChartPojo.class);
            dashboardChartPojo.setDashboardId(newDbId);
            dashboardChartPojo.setId(null);
            dashboardChartPojo.setCreatedAt(ZonedDateTime.now());
            dashboardChartPojo.setUpdatedAt(ZonedDateTime.now());
            dashboardChartPojo.setVersion(0);
            dashboardChartApi.addDashboardChart(dashboardChartPojo);
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    private DashboardPojo duplicateDashboard(DashboardPojo dashboard, Integer orgId) throws ApiException {
        DashboardPojo dashboardPojo = ConvertUtil.convert(dashboard, DashboardPojo.class);
        dashboardPojo.setOrgId(orgId);
        dashboardPojo.setId(null);
        dashboardPojo.setVersion(0);
        dashboardPojo.setCreatedAt(ZonedDateTime.now());
        dashboardPojo.setUpdatedAt(ZonedDateTime.now());
        api.add(dashboardPojo);
        return dashboardPojo;
    }

    /**
     * Returns dashboardIds which have at least one chart of type CUSTOM
     */
    private List<Integer> getCustomDashboardIds(Integer schemaVersionId, List<Integer> dashboardIds) throws ApiException {
        List<DashboardChartPojo> dashboardCharts = dashboardChartApi.getByDashboardIds(dashboardIds);
        Map<Integer, List<String>> dashboardChartAliasMap = dashboardCharts.stream().collect(Collectors.groupingBy(DashboardChartPojo::getDashboardId,
                Collectors.mapping(DashboardChartPojo::getChartAlias, Collectors.toList())));

        List<ReportPojo> charts = reportApi.getByAliasAndSchema(dashboardCharts.stream().map(DashboardChartPojo::getChartAlias).collect(Collectors.toList()),
                schemaVersionId, true);
        Map<String, ReportType> chartAliasReportTypeMap = charts.stream().collect(Collectors.toMap(ReportPojo::getAlias, ReportPojo::getType));


        List<Integer> customDashboardIds = new ArrayList<>();
        for(Integer dashboardId : dashboardIds){
            boolean customChartExists = false;
            for(String chartAlias : dashboardChartAliasMap.getOrDefault(dashboardId, new ArrayList<>())){
                if(!chartAliasReportTypeMap.containsKey(chartAlias))
                    throw new ApiException(ApiStatus.BAD_DATA, "Chart alias not found: " + chartAlias + " for schema version: " + schemaVersionId + " isChart: true");
                if(chartAliasReportTypeMap.get(chartAlias).equals(ReportType.CUSTOM)){
                    customChartExists = true;
                    break;
                }
            }
            if(customChartExists) customDashboardIds.add(dashboardId);
        }
        return customDashboardIds;
    }


}
