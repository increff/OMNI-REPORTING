package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.audit.api.AuditApi;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.common.ConvertUtil;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.FlowApi;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.ChartUtil;
import com.increff.omni.reporting.util.ConstantsUtil;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.increff.omni.reporting.util.ValidateUtil;
import com.nextscm.commons.lang.StringUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.ChartUtil.getChartData;
import static com.increff.omni.reporting.util.ConstantsUtil.DEFAULT_VALUE_COMMON_KEY;
import static com.increff.omni.reporting.util.ConvertUtil.convertChartLegendsPojoToChartLegendsData;
import static com.increff.omni.reporting.util.ValidateUtil.validateDefaultValueForm;

@Service
@Log4j2
@Setter
public class DashboardDto extends AbstractDto {
    @Autowired
    private AuditApi auditApi;
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
    private OrganizationApi organizationApi;
    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private DefaultValueApi defaultValueApi;
    @Autowired
    private ChartLegendsApi chartLegendsApi;
    @Autowired
    private DashboardChartDto dashboardChartDto;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private SchemaVersionApi schemaVersionApi;

    @Autowired
    private FlowApi flowApi;

    public ApplicationPropertiesData getProperties() {
        ApplicationPropertiesData data = new ApplicationPropertiesData();
        data.setMaxDashboardsPerOrg(properties.getMaxDashboardsPerOrg());
        data.setMaxChartsPerDashboard(ValidateUtil.MAX_DASHBOARD_CHARTS);
        return data;
    }

    public FavData getFavoriteDashboard() {
        FavData favData = new FavData();
        FavouritePojo favPojo = api.getFavByOrgUser(getOrgId(), getUserId());
        favData.setUserFav(Objects.nonNull(favPojo) ? ConvertUtil.convert(favPojo, FavouriteData.class) : null);
        favPojo = api.getFavByOrg(getOrgId());
        favData.setOrgFav(Objects.nonNull(favPojo) ? ConvertUtil.convert(favPojo, FavouriteData.class) : null);
        return favData;
    }

    public FavouriteData setUserFavoriteDashboard(FavouriteForm form) {
        FavouritePojo favPojo = ConvertUtil.convert(form, FavouritePojo.class);
        favPojo.setOrgId(getOrgId());
        favPojo.setUserId(getUserId());
        return ConvertUtil.convert(api.setFav(favPojo), FavouriteData.class);
    }

    public FavouriteData setOrgFavoriteDashboard(FavouriteForm form) {
        FavouritePojo favPojo = ConvertUtil.convert(form, FavouritePojo.class);
        favPojo.setOrgId(getOrgId());
        favPojo.setUserId(null);
        return ConvertUtil.convert(api.setFav(favPojo), FavouriteData.class);
    }

    public void deleteFavoriteDashboard(Integer id) {
        api.deleteFavById(id);
        api.saveAudit(id.toString(), AuditActions.DELETE_FAVOURITE.toString(), "Delete Favourite"
                , "Delete Favourite", getUserName());
    }


    @Transactional(rollbackFor = ApiException.class)
    public List<DefaultValueData> upsertDefaultValues(UpsertDefaultValueForm upsertDefaultValueform, Integer dashboardId) throws ApiException {
        List<DefaultValueForm> forms = upsertDefaultValueform.getDefaultValueForms();
        List<DefaultValuePojo> pojos = new ArrayList<>();

        defaultValueApi.deleteByDashboardId(dashboardId); // Delete all existing default values for dashboard

        if(forms.isEmpty()) return new ArrayList<>();
        validateDefaultValueForm(forms, dashboardId);


        runValidationGroupsForDefaultValues(upsertDefaultValueform.getValidationGroupsValueForms());

        for(DefaultValueForm form : forms) {
            api.getCheck(form.getDashboardId(), getOrgId());
            validateControlIdExistsForDashboard(form.getDashboardId(), form.getParamName());

            DefaultValuePojo pojo = ConvertUtil.convert(form, DefaultValuePojo.class);
            pojo.setDefaultValue(String.join(",", form.getDefaultValue()));
            pojos.add(defaultValueApi.upsert(pojo));
        }
        return ConvertUtil.convert(pojos, DefaultValueData.class);
    }

    private void runValidationGroupsForDefaultValues(List<DefaultValueForm> forms) throws ApiException {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        forms.forEach(f-> {
           ReportInputParamsPojo pojo = new ReportInputParamsPojo();
           pojo.setParamKey(f.getParamName());
           if(!f.getDefaultValue().isEmpty()) {
               pojo.setParamValue(f.getDefaultValue().get(0));
               reportInputParamsPojoList.add(pojo);
           }
        });

        List<DashboardChartPojo> dashboardCharts = dashboardChartApi.getByDashboardId(forms.get(0).getDashboardId());
        List<String> chartAliases = dashboardCharts.stream().map(DashboardChartPojo::getChartAlias).collect(Collectors.toList());
        List<Integer> schemaVersionIds = orgMappingApi.getCheckByOrgId(getOrgId()).stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList());
        List<ReportPojo> reports = reportApi.getByAliasAndSchema(chartAliases, schemaVersionIds, true);

        for(ReportPojo report : reports) {
            flowApi.validate(report, reportInputParamsPojoList, flowApi.mergeValidationGroups(report.getId(), reports));
        }
    }


    @Transactional(rollbackFor = ApiException.class)
    public DashboardData addDashboard(DashboardAddForm form) throws ApiException {
        validateDashboardAddForm(form);

        DashboardPojo dashboardPojo = ConvertUtil.convert(form, DashboardPojo.class);

        dashboardPojo.setOrgId(getOrgId());
        api.add(dashboardPojo);
        dashboardChartDto.addDashboardChart(form.getCharts(), dashboardPojo.getId());
        api.saveAudit(dashboardPojo.getId().toString(), "Add Dashboard", AuditActions.ADD_DASHBOARD.name(),
                "Add Dashboard Id: " + dashboardPojo.getId() + " Name: " + dashboardPojo.getName(), getUserName());
        return getDashboard(dashboardPojo.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public DashboardData updateDashboard(DashboardForm form, Integer dashboardId) throws ApiException {
        validateDashboardForm(form);
        api.getCheck(dashboardId, getOrgId());
        DashboardPojo dashboard = api.update(dashboardId, ConvertUtil.convert(form, DashboardPojo.class));
        api.saveAudit(dashboard.getId().toString(), "Update Dashboard", AuditActions.EDIT_DASHBOARD.name(),
                "Update Dashboard Id: " + dashboard.getId() + " Name: " + dashboard.getName(), getUserName());
        return getDashboard(dashboard.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public void deleteDashboard(Integer dashboardId) throws ApiException {
        String dashboardName = api.getCheck(dashboardId, getOrgId()).getName();
        dashboardChartApi.deleteByDashboardId(dashboardId);
        defaultValueApi.deleteByDashboardId(dashboardId);
        api.delete(dashboardId);

        auditApi.save(dashboardId.toString(), AuditActions.DELETE_DASHBOARD.toString(), "Delete DashboardId: " + dashboardId + " Name: " + dashboardName
                , "Delete DashboardId " + dashboardId + " Name: " + dashboardName, getUserName());
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<DashboardListData> getDashboardsByOrgId() throws ApiException {
        return getDashboardsByOrgId(getOrgId());
    }

    public List<DashboardListData> getDashboardsByOrgId(Integer orgId) throws ApiException {
        List<DashboardListData> data = ConvertUtil.convert(api.getByOrgId(orgId), DashboardListData.class);

        List<Integer> allDashboardIds = data.stream().map(DashboardListData::getId).collect(Collectors.toList());

        List<DashboardListData> dashboardListData = new ArrayList<>();
        Set<AppName> appNames = UserPrincipalUtil.getAccessibleApps();
        for (AppName appName : appNames) {
            Set<ReportType> reportTypes = new HashSet<>();
            reportTypes.add(ReportType.STANDARD);
            reportTypes.add(ReportType.CUSTOM);

            if(isCustomReportUser(appName))
                reportTypes.remove(ReportType.STANDARD);
            List<Integer> allowedDashboardIds = getDashboardsWithChartParameters(allDashboardIds, orgId, appNames, reportTypes);
            dashboardListData.addAll(data.stream().filter(dashboard -> allowedDashboardIds.contains(dashboard.getId())).collect(Collectors.toList()));
        }
        // remove duplicates
        dashboardListData = dashboardListData.stream().distinct().collect(Collectors.toList());
        dashboardListData.sort(Comparator.comparing(DashboardListData::getName));
        return dashboardListData;
    }

    @Transactional(rollbackFor = ApiException.class)
    public DashboardData getDashboard(Integer id) throws ApiException {
        DashboardPojo dashboard = api.getCheck(id, getOrgId());
        List<DashboardChartPojo> charts = dashboardChartApi.getCheckByDashboardId(id);

        return ChartUtil.getDashboardData(dashboard.getId(), ConvertUtil.convert(dashboard, DashboardForm.class),
                getFilterDetails(dashboard, charts), getChartLayout(charts, getSchemaVersionIds()));
    }

    @Transactional(rollbackFor = ApiException.class)
    public Map<String,List<InputControlData>> getFilterDetails(DashboardPojo dashboard, List<DashboardChartPojo> charts) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = new HashMap<>();

        setFilterDefaults(dashboard, charts, filterDetails);

        extractCommonFilters(charts, filterDetails);

        sortFiltersForDashboards(filterDetails.get(DEFAULT_VALUE_COMMON_KEY));

        return filterDetails;
    }

    private void setFilterDefaults(DashboardPojo dashboard, List<DashboardChartPojo> charts, Map<String, List<InputControlData>> filterDetails) throws ApiException {
        List<Integer> orgSchemaVersionIds = orgMappingApi.getCheckByOrgId(dashboard.getOrgId()).stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList());

        Map<String, String> paramNameDefaultValueMap = defaultValueApi.getByDashboardId(dashboard.getId())
                .stream().collect(Collectors.toMap(DefaultValuePojo::getParamName, DefaultValuePojo::getDefaultValue));

        for(DashboardChartPojo chart: charts){
            ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), orgSchemaVersionIds, true);

            filterDetails.put(report.getAlias(), new ArrayList<>());
            inputControlDto.selectForReport(report.getId()).forEach(inputControlData -> {
                inputControlData.setDefaultValue(paramNameDefaultValueMap.getOrDefault(inputControlData.getParamName(), null));
                if(Objects.nonNull(inputControlData.getDefaultValue()) && Objects.nonNull(inputControlData.getQuery()))
                    inputControlData.setDefaultValue(String.join(",", getAllowedValuesInDefaults(paramNameDefaultValueMap, inputControlData)));

                filterDetails.get(report.getAlias()).add(inputControlData);
            });
        }
    }

    private List<String> getAllowedValuesInDefaults(Map<String, String> paramNameDefaultValueMap, InputControlData inputControlData) {
        List<String> allowedValues = inputControlData.getOptions().stream().map(InputControlData.InputControlDataValue::getLabelName).collect(Collectors.toList());
        List<String> defaults = getValuesFromList(paramNameDefaultValueMap.get(inputControlData.getParamName()));
        defaults = defaults.stream().filter(allowedValues::contains).collect(Collectors.toList());
        return defaults;
    }

    private List<String> getValuesFromList(String values) {
        if (StringUtil.isEmpty(values))
            return new ArrayList<>();
        return Arrays.asList(values.split(","));
    }

    private void extractCommonFilters(List<DashboardChartPojo> charts, Map<String, List<InputControlData>> filterDetails) {
        // combine all values of each chart into a single list
        List<InputControlData> filters = charts.stream().map(DashboardChartPojo::getChartAlias).map(filterDetails::get).flatMap(List::stream).collect(Collectors.toList());
        // get all param_names
        Set<String> paramNames = filters.stream().map(InputControlData::getParamName).collect(Collectors.toSet());
        // keep first occurrence of each param_name
        Map<String, InputControlData> commonFilters = new HashMap<>();
        for (InputControlData filter : filters) {
            if (!commonFilters.containsKey(filter.getParamName())) {
                commonFilters.put(filter.getParamName(), filter);
            } else {
                combineValidationGroupForFilter(commonFilters.get(filter.getParamName()), filter);
            }
        }
        filterDetails.put(DEFAULT_VALUE_COMMON_KEY, new ArrayList<>(commonFilters.values()));


    }

    private void sortFiltersForDashboards(List<InputControlData> filters) {
        Map<InputControlType, Integer> INPUT_CONTROL_TYPE_SORT_ORDER = new HashMap<>();
        //    ACCESS_CONTROLLED_MULTI_SELECT, DATE, DATE_TIME, MULTI_SELECT, SINGLE_SELECT, NUMBER, TEXT, MULTI_TEXT
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT, 1);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.DATE, 2);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.DATE_TIME, 3);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.MULTI_SELECT, 4);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.SINGLE_SELECT, 5);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.NUMBER, 6);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.TEXT, 7);
        INPUT_CONTROL_TYPE_SORT_ORDER.put(InputControlType.MULTI_TEXT, 8);

        filters.sort((o1, o2) -> {
            if (o1.getType().equals(o2.getType())) {
                if(o1.getType().equals(InputControlType.DATE_TIME)){
                    // o1.dateType START DATE should come before END DATE
                    if(o1.getDateType().equals(DateType.START_DATE) && o2.getDateType().equals(DateType.END_DATE))
                        return -1;
                    else
                        return 1;
                }
                return o1.getDisplayName().compareTo(o2.getDisplayName()); // sort by display name alphabetical
            }
            return INPUT_CONTROL_TYPE_SORT_ORDER.get(o1.getType()) - INPUT_CONTROL_TYPE_SORT_ORDER.get(o2.getType());
        });
    }

    /**
       Merges validation groups of controlB into controlA
     */
    private void combineValidationGroupForFilter(InputControlData controlA, InputControlData controlB) {
        Set<ValidationType> validationTypes = new HashSet<>(controlA.getValidationTypes());
        validationTypes.addAll(controlB.getValidationTypes());
        controlA.setValidationTypes(new ArrayList<>(validationTypes));
    }

    /**
     * @param charts 1D list of charts
     * @return 2D list of charts. Each row is a list of charts for that row in sorted order of col
     */
    private List<List<DashboardGridData>> getChartLayout(List<DashboardChartPojo> charts, List<Integer> schemaVersionIds) throws ApiException {
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
                ReportPojo report = reportApi.getCheckByAliasAndSchema(chart.getChartAlias(), schemaVersionIds, true);
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
        checkValid(form);
        DashboardPojo dashboard = api.getCheck(dashboardId, getOrgId());
        ReportPojo report = reportApi.getCheck(form.getReportId());
        if(!report.getIsEnabled())
            throw new ApiException(ApiStatus.BAD_DATA, "Chart disabled: " + report.getName());
        DashboardChartPojo charts = dashboardChartApi.getCheckByDashboardAndChartAlias(dashboardId, report.getAlias());
        ChartType type = report.getChartType();

        // Get all charts in dashboard. The following is used for merging validation groups. Thus, get all charts irrespective of user accessible charts
        List<ReportPojo> allCharts = reportApi.getByAliasAndSchema(dashboardChartApi.getByDashboardId(dashboardId).stream().map(DashboardChartPojo::getChartAlias).collect(Collectors.toList()),
                orgMappingApi.getCheckByOrgId(dashboard.getOrgId()).stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList()), true);
        List<Map<String, String>> data = reportDto.getLiveData(form, allCharts.stream().map(ReportPojo::getId).collect(Collectors.toList()));

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
            throw new ApiException(ApiStatus.BAD_DATA, "Max limit of dashboards reached: " + properties.getMaxDashboardsPerOrg() +
                    ". Please delete existing dashboards to add new ones");
    }

    private void validateControlIdExistsForDashboard(Integer dashboardId, String paramName) throws ApiException {
        Map<String,List<InputControlData>> filterDetails = getFilterDetails(api.getCheck(dashboardId, getOrgId()),
                dashboardChartApi.getByDashboardId(dashboardId));
        if(filterDetails.values().stream().flatMap(List::stream).noneMatch(inputControlData -> inputControlData.getParamName().equals(paramName))){
            throw new ApiException(ApiStatus.BAD_DATA, "Param Name " + paramName + " does not exist for dashboard");
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
    public void copyDashboardToNewOrgs(List<Integer> orgIds, Boolean copyTestDashboards) throws ApiException {
        // Copies all dashboards created in Increff org (Admin org set in properties file) to new orgs
        List<DashboardPojo> dashboards = api.getByOrgId(properties.getIncreffOrgId());
        for(DashboardPojo dashboard : dashboards) {
            if (dashboard.getName().toLowerCase().startsWith(ConstantsUtil.DASHBOARD_COPY_IGNORE_PREFIX))
                if(!copyTestDashboards) continue; // Skip if dashboard name starts with test

            copyDashboardToSomeOrgs(dashboard.getId(), dashboard.getOrgId(), orgIds);
        }
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
    private List<Integer> getCustomDashboardIds(List<Integer> schemaVersionIds, List<Integer> dashboardIds) throws ApiException {
        List<DashboardChartPojo> dashboardCharts = dashboardChartApi.getByDashboardIds(dashboardIds);
        Map<Integer, List<String>> dashboardChartAliasMap = dashboardCharts.stream().collect(Collectors.groupingBy(DashboardChartPojo::getDashboardId,
                Collectors.mapping(DashboardChartPojo::getChartAlias, Collectors.toList())));

        List<ReportPojo> charts = reportApi.getByAliasAndSchema(dashboardCharts.stream().map(DashboardChartPojo::getChartAlias).collect(Collectors.toList()),
                schemaVersionIds, true);
        Map<String, ReportType> chartAliasReportTypeMap = charts.stream().collect(Collectors.toMap(ReportPojo::getAlias, ReportPojo::getType));


        List<Integer> customDashboardIds = new ArrayList<>();
        for(Integer dashboardId : dashboardIds){
            boolean customChartExists = false;
            for(String chartAlias : dashboardChartAliasMap.getOrDefault(dashboardId, new ArrayList<>())){
                if(!chartAliasReportTypeMap.containsKey(chartAlias))
                    throw new ApiException(ApiStatus.BAD_DATA, "Chart alias not found: " + chartAlias + " for schema version: " + schemaVersionIds + " isChart: true");
                if(chartAliasReportTypeMap.get(chartAlias).equals(ReportType.CUSTOM)){
                    customChartExists = true;
                    break;
                }
            }
            if(customChartExists) customDashboardIds.add(dashboardId);
        }
        return customDashboardIds;
    }


    /**
     * Returns dashboardIds containing at least 1 chart with given chart parameters
     */
    private List<Integer> getDashboardsWithChartParameters(List<Integer> dashboardIds, Integer orgId, Set<AppName> appNames, Set<ReportType> reportTypes) throws ApiException {
        log.debug("getDashboardsWithChartParameters.input orgId: " + orgId + " appNames: " + appNames + " reportTypes: " + reportTypes + " dashboardIds: " + dashboardIds);
        List<Integer> userAllowedSchemaVersionIds = schemaVersionApi.getByAppNames(appNames).
                stream().map(SchemaVersionPojo::getId).collect(Collectors.toList());
        List<Integer> orgSchemaVersionIds = orgMappingApi.getCheckByOrgId(orgId).stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList());

        List<DashboardChartPojo> dashboardCharts = dashboardChartApi.getByDashboardIds(dashboardIds);
        Map<Integer, List<String>> dashboardChartAliasMap = dashboardCharts.stream().collect(Collectors.groupingBy(DashboardChartPojo::getDashboardId,
                Collectors.mapping(DashboardChartPojo::getChartAlias, Collectors.toList())));

        List<ReportPojo> charts = reportApi.getByAliasAndSchema(dashboardCharts.stream().map(DashboardChartPojo::getChartAlias).collect(Collectors.toList()),
                orgSchemaVersionIds, true);
        Map<String, ReportPojo> chartAliasChartMap = charts.stream().collect(Collectors.toMap(ReportPojo::getAlias, report -> report));


        List<Integer> dashboardIdsWithUserAccessSchemaVersion = new ArrayList<>();
        for(Integer dashboardId : dashboardIds){
            boolean appChartExists = false;
            for(String chartAlias : dashboardChartAliasMap.getOrDefault(dashboardId, new ArrayList<>())){
                if(!chartAliasChartMap.containsKey(chartAlias))
                    throw new ApiException(ApiStatus.BAD_DATA, "Chart alias not found: " + chartAlias + " for schema version: " + orgSchemaVersionIds + " isChart: true");
                if(userAllowedSchemaVersionIds.contains(chartAliasChartMap.get(chartAlias).getSchemaVersionId())
                && reportTypes.contains(chartAliasChartMap.get(chartAlias).getType())){
                    appChartExists = true;
                    log.debug("getDashboardsWithChartParameters.found dashboardId: " + dashboardId + " chartAlias: " + chartAlias);
                    break;
                }
            }
            if(appChartExists) dashboardIdsWithUserAccessSchemaVersion.add(dashboardId);
        }
        log.debug("getDashboardsWithChartParameters.return " + dashboardIdsWithUserAccessSchemaVersion);
        return dashboardIdsWithUserAccessSchemaVersion;
    }




}
