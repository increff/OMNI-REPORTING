package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.common.ConvertUtil;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.SqlCmd;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.increff.omni.reporting.util.ValidateUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getDirectoryPath;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getIdToPojoMap;
import static com.increff.omni.reporting.util.ChartUtil.getChartData;
import static com.increff.omni.reporting.util.ConvertUtil.convertChartLegendsPojoToChartLegendsData;
import static com.increff.omni.reporting.util.MongoUtil.COLLECTION_NAME;
import static com.increff.omni.reporting.util.ValidateUtil.validateReportForm;

@Service
@Setter
@Log4j2
public class ReportDto extends AbstractDto {

    @Autowired
    private ReportFlowApi flowApi;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportRequestFlowApi reportRequestFlowApi;
    @Autowired
    private ReportQueryApi reportQueryApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private ChartLegendsApi chartLegendsApi;
    @Autowired
    private InputControlFlowApi inputControlFlowApi;

    public ReportData add(ReportForm form) throws ApiException {
        validateReportForm(form);
        form.setAlias(form.getAlias().trim().toLowerCase());
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo = flowApi.addReport(pojo, form.getLegends());
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.CREATE_REPORT.toString()
                , "Create Report", "Report : " + pojo.getName() + " created", getUserName());
        ReportData reportData = convertToReportData(Collections.singletonList(pojo)).get(0);
        reportData.setLegends(convertChartLegendsPojoToChartLegendsData(chartLegendsApi.getByChartId(pojo.getId())).getLegends());
        return reportData;
    }

    public ReportData edit(Integer id, ReportForm form) throws ApiException {
        validateReportForm(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo.setId(id);
        pojo = flowApi.editReport(pojo, form.getLegends());
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.EDIT_REPORT.toString()
                , "Update Report", "Report : " + pojo.getName() + " updated", getUserName());
        ReportData reportData = convertToReportData(Collections.singletonList(pojo)).get(0);
        reportData.setLegends(convertChartLegendsPojoToChartLegendsData(chartLegendsApi.getByChartId(pojo.getId())).getLegends());
        return reportData;
    }

    public List<Map<String, String>> getLiveDataForAnyOrganization(ReportRequestForm form, Integer orgId, List<Integer> valGroupMergeReportIds)
            throws ApiException, IOException {
        OrganizationPojo organizationPojo = organizationApi.getCheck(orgId);
        ReportPojo reportPojo = reportApi.getCheck(form.getReportId());

        validateReportForOrg(reportPojo, orgId);
        validateQueryExists(reportPojo, form);

        OrgMappingPojo orgMappingPojo = orgMappingApi.getCheckByOrgIdSchemaVersionId(orgId, reportPojo.getSchemaVersionId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgMappingPojo.getConnectionId());
        String password = getDecryptedPassword(connectionPojo.getPassword());

        ZonedDateTime startTime = ZonedDateTime.now();
        try {
            List<ReportInputParamsPojo> reportInputParamsPojoList = validateControls(form, orgId, reportPojo, password);
            return flowApi.validateAndGetLiveData(reportPojo, reportInputParamsPojoList, connectionPojo, password, form.getQuery(),
                    reportApi.getByIds(valGroupMergeReportIds));
        } finally {
            flowApi.saveAudit(reportPojo.getId().toString(), AuditActions.LIVE_REPORT.toString(),
                    "Live Report",
                    "Live Report request submitted for organization : " + organizationPojo.getName()
                            + " , duration : " + (int) ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                            + " , reportId : " + form.getReportId(), getUserName());
        }
    }

    public List<Map<String, String>> getLiveData(ReportRequestForm form, List<Integer> valGroupsMergeReportIds) throws ApiException, IOException {
        return getLiveDataForAnyOrganization(form, getOrgId(), valGroupsMergeReportIds);
    }

    public void updateStatus(Integer reportId, Boolean isEnabled) throws ApiException {
        ReportPojo pojo = reportApi.getCheck(reportId);
        pojo.setIsEnabled(isEnabled);
        reportApi.edit(pojo);
    }

    public ReportData get(Integer id) throws ApiException {
        ReportPojo pojo = reportApi.getCheck(id);
        ReportData reportData = convertToReportData(Collections.singletonList(pojo)).get(0);
        reportData.setLegends(convertChartLegendsPojoToChartLegendsData(chartLegendsApi.getByChartId(pojo.getId())).getLegends());
        return reportData;
    }

    public ReportQueryData upsertQuery(Integer reportId, ReportQueryForm form) throws ApiException {
        checkValid(form);
        ReportQueryPojo pojo = ConvertUtil.convert(form, ReportQueryPojo.class);
        pojo.setReportId(reportId);

        ValidateUtil.validateReportQueryForm(form, getReportSV(reportId).getAppName());

        ReportQueryPojo oldPojo = reportQueryApi.getByReportId(reportId);
        String oldQuery = Objects.isNull(oldPojo) ? "" : oldPojo.getQuery();
        pojo = flowApi.upsertQuery(pojo);
        String newQuery = pojo.getQuery();
        flowApi.saveAudit(reportId.toString(), AuditActions.UPSERT_REPORT_QUERY.toString(), "Upsert Report Query"
                , "Report query updated. Old query\n" + oldQuery + "\nNew Query\n" + newQuery, getUserName());
        return ConvertUtil.convert(pojo, ReportQueryData.class);
    }

    public ReportQueryData getTransformedQuery(ReportQueryTestForm form) throws ApiException {
        Map<String, String> paramsMap = UserPrincipalUtil.getMapWithoutAccessControl(form.getParamMap());
        paramsMap.put("timezone", "'" + form.getTimezone() + "'");
        ReportQueryData data = new ReportQueryData();

        data.setQuery(SqlCmd.getFinalQuery(paramsMap, form.getQuery(), true, getDBType(form.getQuery())));
        return data;
    }

    private static DBType getDBType(String query) {
        // if query contains collection name, it is mongo
        if (query.contains(COLLECTION_NAME))
            return DBType.MONGO;
        return DBType.MYSQL;
    }

    public ReportQueryData getQuery(Integer reportId) throws ApiException {
        reportApi.getCheck(reportId);
        ReportQueryData data = new ReportQueryData();
        ReportQueryPojo queryPojo = reportQueryApi.getByReportId(reportId);
        data.setQuery(Objects.isNull(queryPojo) ? "" : queryPojo.getQuery());
        data.setUpdatedAt(Objects.isNull(queryPojo) ? null : queryPojo.getUpdatedAt());
        return data;
    }

    public TestQueryLiveData testQueryLive(ReportRequestForm form, Integer orgId) throws IOException, ApiException {
        ReportPojo report = reportApi.getCheck(form.getReportId());
        Integer schemaVersionId = report.getSchemaVersionId();
        log.debug("Testing query on orgId : " + orgId + " schemaVersionId : " + schemaVersionId + " reportName : " + report.getName() + " reportId : " + report.getId());

        List<Map<String, String>> data = getLiveDataForAnyOrganization(form, orgId, Collections.singletonList(form.getReportId()));
        ChartInterface chartInterface = getChartData(report.getChartType());
        chartInterface.validateNormalize(data, report.getChartType());

        return getTestQueryLiveData(report, data, chartInterface, orgId);
    }

    private TestQueryLiveData getTestQueryLiveData(ReportPojo report, List<Map<String, String>> data, ChartInterface chartInterface,
                                Integer orgId) throws ApiException {
        ViewDashboardData viewData = new ViewDashboardData();
        viewData.setChartData(chartInterface.transform(data));
        viewData.setLegends(convertChartLegendsPojoToChartLegendsData(chartLegendsApi.getByChartId(report.getId())).getLegends());
        viewData.setChartId(report.getId());
        viewData.setType(report.getChartType());

        TestQueryLiveData testQueryLiveData = new TestQueryLiveData();
        testQueryLiveData.setViewDashboardData(Collections.singletonList(viewData)); // converting to list for UI
        testQueryLiveData.setTestedSchemaVersionId(report.getSchemaVersionId());
        testQueryLiveData.setTestedOrgId(orgId);

        OrgMappingPojo orgMappingPojo = orgMappingApi.getCheckByOrgIdSchemaVersionId(orgId, report.getSchemaVersionId());
        testQueryLiveData.setTestedConnectionId(orgMappingPojo.getConnectionId());
        return testQueryLiveData;
    }

    public ReportData selectByAlias(Boolean isChart, String alias) throws ApiException {
        Integer orgId = getOrgId();
        organizationApi.getCheck(orgId);

        List<OrgMappingPojo> orgMappingPojos = orgMappingApi.getCheckByOrgId(orgId);
        List<Integer> orgSchemaVersionIds = orgMappingPojos.stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList());
        ReportPojo reportPojo = reportApi.getByAliasAndSchema(alias, orgSchemaVersionIds,
                isChart);
        if(Objects.isNull(reportPojo))
            throw new ApiException(ApiStatus.BAD_DATA,
                    (isChart ? "Dashboard" : "Report")  + " not available for alias : " + alias);
        validateCustomReportAccess(reportPojo, orgId);
        return convertToReportData(Collections.singletonList(reportPojo)).get(0);
    }

    public List<ReportData> selectByOrg(Boolean isChart, VisualizationType visualization) throws ApiException {
        return selectByOrg(getOrgId(), isChart, visualization);
    }

    public List<ReportData> selectByOrg(Integer orgId, Boolean isChart, VisualizationType visualization) throws ApiException {
        organizationApi.getCheck(orgId);
        List<ReportPojo> pojos = flowApi.getAll(orgId, isChart, visualization);
        return convertToReportData(pojos);
    }

    public List<ReportData> selectAllBySchemaVersion(Integer schemaVersionId, VisualizationType visualization) throws ApiException {
        List<ReportPojo> pojos = flowApi.getAllBySchemaVersionId(schemaVersionId, visualization);
        return convertToReportData(pojos);
    }

    public void mapToControl(Integer reportId, Integer controlId) throws ApiException {
        if (reportId == null || controlId == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report id or control id cannot be null");
        ReportControlsPojo pojo = CommonDtoHelper.getReportControlPojo(reportId, controlId, inputControlFlowApi.getMaxSortOrder(reportId) + 1);
        flowApi.mapControlToReport(pojo);
    }

    public void updateReportControlMappingSortOrder(Integer reportId, List<Integer> controlIds) throws ApiException {
        flowApi.updateReportControlMappingSortOrder(reportId, controlIds);
    }

    public void deleteReportControl(Integer reportId, Integer controlId) throws ApiException {
        flowApi.deleteReportControl(reportId, controlId);
    }

    public void addValidationGroup(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        validate(reportId, groupForm);
        flowApi.addValidationGroup(reportId, groupForm);
    }

    public void deleteValidationGroup(Integer reportId, String groupName) throws ApiException {
        reportValidationGroupApi.deleteByReportIdAndGroupName(reportId, groupName);
    }

    public void copyReports(CopyReportsForm form) throws ApiException {
        flowApi.copyReports(form.getOldSchemaVersionId(), form.getNewSchemaVersionId());
    }

    public List<ValidationGroupData> getValidationGroups(Integer reportId) {
        List<ValidationGroupData> validationGroupDataList = new ArrayList<>();
        List<ReportValidationGroupPojo> validationGroupPojoList = reportValidationGroupApi.getByReportId(reportId);
        Map<String, List<ReportValidationGroupPojo>> groupedByName = validationGroupPojoList.stream()
                .collect(Collectors.groupingBy(ReportValidationGroupPojo::getGroupName));
        groupedByName.forEach((k, v) -> {
            ValidationGroupData data = new ValidationGroupData();
            List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByIds(v.stream()
                    .map(ReportValidationGroupPojo::getReportControlId).collect(Collectors.toList()));
            List<Integer> controlIds = reportControlsPojoList.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
            List<InputControlPojo> pojos = inputControlApi.selectByIds(controlIds);
            data.setValidationValue(v.get(0).getValidationValue());
            data.setIsSystemValidation(v.get(0).getIsSystemValidation());
            data.setGroupName(k);
            data.setValidationType(v.get(0).getType());
            data.setControls(pojos.stream().map(InputControlPojo::getDisplayName).collect(Collectors.toList()));
            data.setCanDelete(!hasAccessControlledMultiSelect(pojos));
            validationGroupDataList.add(data);
        });
        return validationGroupDataList;
    }

    private boolean hasAccessControlledMultiSelect(List<InputControlPojo> pojos) {
        return pojos.stream().anyMatch(pojo -> pojo.getType().equals(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT));
    }

    private void validate(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        checkValid(groupForm);
        if (Objects.isNull(reportId))
            throw new ApiException(ApiStatus.BAD_DATA, "Report id cannot be null");
        if (groupForm.getControlIds().stream().distinct().count() != groupForm.getControlIds().size())
            throw new ApiException(ApiStatus.BAD_DATA, "Validation group contains duplicate control ids");
        if (groupForm.getValidationType().equals(ValidationType.DATE_RANGE) && groupForm.getValidationValue() <= 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Date range validation should have positive validation value");
    }

    private List<ReportData> convertToReportData(List<ReportPojo> pojos) throws ApiException {
        List<ReportData> dataList = new ArrayList<>();
        if(pojos.isEmpty())
            return dataList;
        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getCheck(pojos.get(0).getSchemaVersionId());
        List<DirectoryPojo> directoryPojoList = directoryApi.getAll();
        Map<Integer, DirectoryPojo> idToDirectoryPojoList = getIdToPojoMap(directoryPojoList);
        for(ReportPojo p : pojos) {
            ReportData data = ConvertUtil.convert(p, ReportData.class);
            DirectoryPojo directoryPojo = idToDirectoryPojoList.get(data.getDirectoryId());
            data.setDirectoryName(directoryPojo.getDirectoryName());
            data.setDirectoryPath(getDirectoryPath(directoryPojo.getId(), idToDirectoryPojoList));
            data.setSchemaVersionName(schemaVersionPojo.getName());
            if(Objects.isNull(data.getMinFrequencyAllowedSeconds())) data.setMinFrequencyAllowedSeconds(0);
            dataList.add(data);
        }
        return dataList;
    }

    private List<ReportInputParamsPojo> validateControls(ReportRequestForm form, Integer orgId,
                                                         ReportPojo reportPojo, String password) throws ApiException {
        Map<String, String> inputParamsMap = UserPrincipalUtil.getMapWithoutAccessControl(form.getParamMap());
        Map<String, List<String>> inputDisplayMap = new HashMap<>();
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = inputControlApi.selectByIds(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        validateInputParamValues(form.getParamMap(), inputParamsMap, orgId, inputDisplayMap, inputControlPojoList,
                ReportRequestType.USER, password);
        Map<String, String> inputDisplayStringMap = UserPrincipalUtil.getStringToStringParamMap(inputDisplayMap);
        return CommonDtoHelper.getReportInputParamsPojoList(inputParamsMap, form.getTimezone(), orgId, inputDisplayStringMap);
    }

    private ReportPojo validateReportForOrg(ReportPojo reportPojo, Integer orgId) throws ApiException {
        validateCustomReportAccess(reportPojo, orgId);
        if(!reportPojo.getIsChart())
            throw new ApiException(ApiStatus.BAD_DATA, "Live data is only available for dashboards");
        return reportPojo;
    }

    private void validateQueryExists(ReportPojo reportPojo, ReportRequestForm form) throws ApiException {
        ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
        if (Objects.isNull(reportQueryPojo) && Objects.isNull(form.getQuery()))
            throw new ApiException(ApiStatus.BAD_DATA, "No query defined for report : " + reportPojo.getName());
    }

    private SchemaVersionPojo getReportSV(Integer reportId) throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(reportId);
        return schemaVersionApi.getCheck(reportPojo.getSchemaVersionId());
    }


}
