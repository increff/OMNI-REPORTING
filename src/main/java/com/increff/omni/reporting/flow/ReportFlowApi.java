package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.MongoUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.AbstractDto.isCustomReportUser;
import static com.increff.omni.reporting.dto.CommonDtoHelper.*;

@Service
@Setter
@Log4j
public class ReportFlowApi extends AbstractFlowApi {

    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private DBConnectionApi dbConnectionApi;
    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private CustomReportAccessApi customReportAccessApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private MandatoryValidator mandatoryValidator;
    @Autowired
    private DateValidator dateValidator;
    @Autowired
    private SingleMandatoryValidator singleMandatoryValidator;
    @Autowired
    private ReportApi api;
    @Autowired
    private ReportQueryApi queryApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private ReportScheduleApi reportScheduleApi;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ChartLegendsApi chartLegendsApi;
    @Autowired
    private ApplicationProperties properties;

    private static final Integer MAX_NUMBER_OF_ROWS = 300;

    @Transactional(rollbackFor = ApiException.class)
    public ReportPojo addReport(ReportPojo pojo, Map<String, String> legends) throws ApiException {
        validate(pojo);
        api.add(pojo);
        chartLegendsApi.put(pojo.getId(), legends);
        return pojo;
    }

    @Transactional(rollbackFor = ApiException.class)
    public ReportPojo editReport(ReportPojo pojo, Map<String, String> legends) throws ApiException {
        ReportPojo existing = api.getCheck(pojo.getId());
        List<Integer> orgIds = orgMappingApi.getBySchemaVersionId(pojo.getSchemaVersionId()).stream().map(OrgMappingPojo::getOrgId).collect(Collectors.toList());
        validateForEdit(pojo, reportScheduleApi.selectByOrgIdReportAlias(orgIds, pojo.getAlias()));
        if(existing.getChartType() != pojo.getChartType())
            throw new ApiException(ApiStatus.BAD_DATA, "Chart type can't be changed." +
                    " Current : " + existing.getChartType() + " New : " + pojo.getChartType());
        // Delete custom report access if transition is happening from CUSTOM to STANDARD
        if (existing.getType().equals(ReportType.CUSTOM) && pojo.getType().equals(ReportType.STANDARD))
            customReportAccessApi.deleteByReportId(pojo.getId());
        chartLegendsApi.put(pojo.getId(), legends);
        return api.edit(pojo);
    }

    public List<Map<String, String>> validateAndGetLiveData(ReportPojo reportPojo,
                                                            List<ReportInputParamsPojo> reportInputParamsPojoList,
                                                            ConnectionPojo connectionPojo, String password, String query)
            throws ApiException, IOException {
        validate(reportPojo, reportInputParamsPojoList);

        if(Objects.isNull(query))
            query = queryApi.getByReportId(reportPojo.getId()).getQuery();
        File file = folderApi.getFileForExtension(reportPojo.getId(), ".csv");
        // Creation of file
        Connection connection = null;
        try {
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            String fQuery = SqlCmd.getFinalQuery(inputParamMap, query, true);
            int noOfRows = 0;
            if(connectionPojo.getDbType().equals(DBType.MYSQL)) {
                connection = dbConnectionApi.getConnection(connectionPojo.getHost(), connectionPojo.getUsername(),
                        password, properties.getMaxConnectionTime());
                PreparedStatement statement = dbConnectionApi.getStatement(connection,
                        properties.getLiveReportMaxExecutionTime(), fQuery, properties.getResultSetFetchSize());
                ResultSet resultSet = statement.executeQuery();
                noOfRows = FileUtil.writeCsvFromResultSet(resultSet, file);
            } else if (connectionPojo.getDbType().equals(DBType.MONGO)) {
                List<Document> docs = MongoUtil.executeMongoPipeline(connectionPojo.getHost(), connectionPojo.getUsername(),
                        password, fQuery);
                noOfRows = FileUtil.writeCsvFromMongoDocuments(docs, file);
            }


            if (noOfRows > MAX_NUMBER_OF_ROWS) {
                throw new ApiException(ApiStatus.BAD_DATA, "Data exceeded " + MAX_NUMBER_OF_ROWS + " Rows, select " +
                        "granular filters.");
            }
            return FileUtil.getJsonDataFromFile(file, ',');
        } catch (Exception e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Failed to get the data for dashboard : " + e.getMessage());
        } finally {
            FileUtil.delete(file);
            try {
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public ReportQueryPojo upsertQuery(ReportQueryPojo pojo) throws ApiException {
        api.getCheck(pojo.getReportId());
        return queryApi.upsertQuery(pojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void mapControlToReport(ReportControlsPojo pojo) throws ApiException {
        validateControlReportMapping(pojo);
        reportControlsApi.add(pojo);
        checkAndAddValidationGroup(pojo);
    }

    @Transactional(readOnly = true)
    public List<ReportPojo> getAllBySchemaVersionId(Integer schemaVersionId, VisualizationType visualization) {
        return api.getBySchemaVersion(schemaVersionId, visualization);
    }

    @Transactional(readOnly = true)
    public List<ReportPojo> getAll(Integer orgId, Boolean isChart, VisualizationType visualization) throws ApiException {

        // get intersection of schema version ids accessible to user and schema version ids mapped to org
        List<OrgMappingPojo> orgMappingPojo = orgMappingApi.getCheckByOrgId(orgId);
        List<Integer> orgUserSchemaVersionIds = orgMappingPojo.stream().map(OrgMappingPojo::getSchemaVersionId)
                .collect(Collectors.toList());
        List<Integer> userAllowedSchemaVersionIds = schemaVersionApi.getByAppNames(UserPrincipalUtil.getAccessibleApps()).stream()
                .map(SchemaVersionPojo::getId).collect(Collectors.toList());
        orgUserSchemaVersionIds.retainAll(userAllowedSchemaVersionIds);

        List<ReportPojo> reportPojoList = new ArrayList<>();
        //All standard
        List<ReportPojo> standard =
                api.getByTypeAndSchema(ReportType.STANDARD, orgUserSchemaVersionIds, isChart, visualization);

        //All custom
        List<CustomReportAccessPojo> customAccess = customReportAccessApi.getByOrgId(orgId);
        List<Integer> customIds = customAccess.stream().map(CustomReportAccessPojo::getReportId)
                .collect(Collectors.toList());

        List<ReportPojo> custom =
                api.getByIdsAndSchema(customIds, orgUserSchemaVersionIds, isChart);

        if(!isCustomReportUser()) // Add standard reports only if user in not custom report user
            reportPojoList.addAll(standard);
        reportPojoList.addAll(custom);
        return reportPojoList;
    }

    @Transactional(rollbackFor = ApiException.class)
    public void addValidationGroup(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        validate(reportId, groupForm);
        List<InputControlPojo> inputControlPojoList = inputControlApi.selectByIds(groupForm.getControlIds());
        List<InputControlType> controlTypeList =
                inputControlPojoList.stream().map(InputControlPojo::getType).collect(Collectors.toList());
        switch (groupForm.getValidationType()) {
            case MANDATORY:
                mandatoryValidator.add(controlTypeList);
                break;
            case DATE_RANGE:
                dateValidator.add(controlTypeList);
                break;
            case SINGLE_MANDATORY:
                singleMandatoryValidator.add(controlTypeList);
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Invalid Validation Type");
        }
        List<ReportValidationGroupPojo> validationGroupPojoList = getValidationGroupPojoList(groupForm, reportId);
        validationGroupPojoList.forEach(v -> v.setIsSystemValidation(false));
        reportValidationGroupApi.addAll(validationGroupPojoList);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void deleteReportControl(Integer reportId, Integer controlId) throws ApiException {
        api.getCheck(reportId);
        ReportControlsPojo pojo = reportControlsApi.getByReportAndControlId(reportId, controlId);
        checkNotNull(pojo, "No report control exist with control id : " + controlId);
        reportValidationGroupApi.deleteByReportIdAndReportControlId(reportId, pojo.getId());
        reportControlsApi.delete(pojo.getId());
    }

    @Transactional(rollbackFor = ApiException.class)
    public void copyReports(Integer oldSchemaVersionId, Integer newSchemaVersionId) throws ApiException {
        schemaVersionApi.getCheck(oldSchemaVersionId);
        schemaVersionApi.getCheck(newSchemaVersionId);
        // Migrate Input controls
        Map<Integer, Integer> oldToNewControlIds = migrateInputControls(oldSchemaVersionId, newSchemaVersionId);
        // Migrate Reports
        List<ReportPojo> oldSchemaReports = api.getBySchemaVersion(oldSchemaVersionId, null);
        for (ReportPojo oldReport : oldSchemaReports) {
            ReportPojo ex = api.getByNameAndSchema(oldReport.getName(), newSchemaVersionId, oldReport.getIsChart());
            if (Objects.nonNull(ex))
                continue;
            // Add Report
            ReportPojo pojo = getReportPojoFromExistingPojo(oldReport);
            pojo.setSchemaVersionId(newSchemaVersionId);
            addReport(pojo, new HashMap<>());

            // Copy Legends
            List<ChartLegendsPojo> legendsPojoList = chartLegendsApi.getByChartId(oldReport.getId());
            chartLegendsApi.put(pojo.getId(), legendsPojoList.stream().collect(Collectors.toMap(ChartLegendsPojo::getLegendKey, ChartLegendsPojo::getValue)));

            // Add Report Query
            ReportQueryPojo exQuery = queryApi.getByReportId(oldReport.getId());
            if (Objects.nonNull(exQuery)) {
                ReportQueryPojo queryPojo = new ReportQueryPojo();
                queryPojo.setQuery(exQuery.getQuery());
                queryPojo.setReportId(pojo.getId());
                queryApi.upsertQuery(queryPojo);
            }
            // Add Report Controls
            Map<Integer, Integer> reportControlIdMap = new HashMap<>();
            List<ReportControlsPojo> controlsPojos = reportControlsApi.getByReportId(oldReport.getId());
            for (ReportControlsPojo c : controlsPojos) {
                ReportControlsPojo p = new ReportControlsPojo();
                p.setControlId(oldToNewControlIds.get(c.getControlId()));
                p.setReportId(pojo.getId());
                reportControlsApi.add(p);
                reportControlIdMap.put(c.getId(), p.getId());
            }
            // Add Report Validation Groups
            List<ReportValidationGroupPojo> groupPojoList = reportValidationGroupApi.getByReportId(oldReport.getId());
            List<ReportValidationGroupPojo> newGroupPojoList = new ArrayList<>();
            for (ReportValidationGroupPojo v : groupPojoList) {
                ReportValidationGroupPojo p = getValidationGroupPojoFromExistingPojo(v);
                p.setReportId(pojo.getId());
                p.setReportControlId(reportControlIdMap.get(v.getReportControlId()));
                newGroupPojoList.add(p);
            }
            reportValidationGroupApi.addAll(newGroupPojoList);
            // Add custom report access
            if (oldReport.getType().equals(ReportType.STANDARD))
                continue;
            List<CustomReportAccessPojo> customReportAccessPojoList =
                    customReportAccessApi.getAllByReportId(oldReport.getId());
            customReportAccessPojoList.forEach(c -> {
                CustomReportAccessPojo customReportAccessPojo = new CustomReportAccessPojo();
                customReportAccessPojo.setOrgId(c.getOrgId());
                customReportAccessPojo.setReportId(pojo.getId());
                customReportAccessApi.addCustomReportAccessPojo(customReportAccessPojo);
            });
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public void checkAndAddValidationGroup(ReportControlsPojo pojo) throws ApiException {
        InputControlPojo inputControlPojo = inputControlApi.getCheck(pojo.getControlId());
        if (Arrays.asList(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT, InputControlType.SINGLE_SELECT)
                .contains(inputControlPojo.getType())) {
            ReportValidationGroupPojo validationGroupPojo = new ReportValidationGroupPojo();
            validationGroupPojo.setGroupName(inputControlPojo.getDisplayName() + " " +
                    ValidationType.MANDATORY.toString().toLowerCase());
            validationGroupPojo.setValidationValue(0);
            validationGroupPojo.setIsSystemValidation(true);
            validationGroupPojo.setReportId(pojo.getReportId());
            validationGroupPojo.setReportControlId(pojo.getId());
            validationGroupPojo.setType(ValidationType.MANDATORY);
            reportValidationGroupApi.addAll(Collections.singletonList(validationGroupPojo));
        }
    }

    private void validate(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        api.getCheck(reportId);
        for (Integer controlId : groupForm.getControlIds()) {
            ReportControlsPojo pojo = reportControlsApi.getByReportAndControlId(reportId, controlId);
            checkNotNull(pojo, "No report control exist with control id : " + controlId);
        }
        List<ReportValidationGroupPojo> pojoList = reportValidationGroupApi.getByNameAndReportId(reportId
                , groupForm.getGroupName());
        if (!pojoList.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "Group name already exist for given report, group name : "
                    + groupForm.getGroupName());
    }

    private void validateControlReportMapping(ReportControlsPojo pojo) throws ApiException {
        //valid report
        ReportPojo reportPojo = api.getCheck(pojo.getReportId());
        //valid control - Global
        InputControlPojo icPojo = inputControlApi.getCheck(pojo.getControlId());
        if (icPojo.getScope().equals(InputControlScope.LOCAL))
            throw new ApiException(ApiStatus.BAD_DATA, "Only Global Control can be mapped to a report");
        if (!icPojo.getSchemaVersionId().equals(reportPojo.getSchemaVersionId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report Schema version and input control schema version not " +
                    "matching");
    }

    private void validateForEdit(ReportPojo pojo, List<ReportSchedulePojo> reportSchedulePojos) throws ApiException {
        if(pojo.getCanSchedule())
            validateCronFrequency(pojo, reportSchedulePojos);

        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        // validating if requested name is already present
        ReportPojo existingByName =
                api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId(), pojo.getIsChart());
        if (Objects.nonNull(existingByName) && !existingByName.getId().equals(pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name, schema version and " +
                    "report type (normal / dashboard)");
    }

    private void validate(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        List<SchemaVersionPojo> aliasSchemas = schemaVersionApi.getByIds(api.getByAlias(pojo.getAlias()).stream()
                .map(ReportPojo::getSchemaVersionId).collect(Collectors.toList()));
        List<AppName> existingAppNames = aliasSchemas.stream().map(SchemaVersionPojo::getAppName).collect(Collectors.toList());
        AppName curentAppName = schemaVersionApi.getCheck(pojo.getSchemaVersionId()).getAppName();
        if (existingAppNames.size() > 1)
            throw new ApiException(ApiStatus.BAD_DATA, "Alias " + pojo.getAlias() + " exists across app names!!"); // This should never occur. If this is hit, something disastrous has happened
        if( (existingAppNames.size() == 1) && (existingAppNames.get(0) != curentAppName) )
            throw new ApiException(ApiStatus.BAD_DATA, "Alias " + pojo.getAlias() + " is already present in different app." +
                    " CurrentAppName : " + curentAppName + " ExistingAppName : " + existingAppNames.get(0));

        if(StringUtil.isEmpty(pojo.getAlias()) || pojo.getAlias().contains(" "))
            throw new ApiException(ApiStatus.BAD_DATA, "Report alias can't have space, use underscore(_) instead");
        ReportPojo existing = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId(), pojo.getIsChart());
        if (existing != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name, schema version and " +
                    "report type (normal / dashboard)");
        ReportPojo ex = api.getByAliasAndSchema(pojo.getAlias(), Collections.singletonList(pojo.getSchemaVersionId()), pojo.getIsChart());
        if (ex != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same alias, schema version and " +
                    "report type (normal / dashboard)");
    }

    private List<ReportValidationGroupPojo> getValidationGroupPojoList(ValidationGroupForm groupForm,
                                                                       Integer reportId) {
        List<ReportValidationGroupPojo> groupPojoList = new ArrayList<>();
        for (Integer controlId : groupForm.getControlIds()) {
            ReportValidationGroupPojo pojo = new ReportValidationGroupPojo();
            pojo.setGroupName(groupForm.getGroupName());
            pojo.setReportId(reportId);
            ReportControlsPojo controlsPojo = reportControlsApi.getByReportAndControlId(reportId, controlId);
            pojo.setReportControlId(controlsPojo.getId());
            pojo.setType(groupForm.getValidationType());
            pojo.setValidationValue(groupForm.getValidationValue());
            groupPojoList.add(pojo);
        }
        return groupPojoList;
    }

    private Map<Integer, Integer> migrateInputControls(Integer oldSchemaVersionId, Integer newSchemaVersionId)
            throws ApiException {
        Map<Integer, Integer> oldToNewControls = new HashMap<>();
        List<InputControlPojo> oldControls = inputControlApi.getBySchemaVersion(oldSchemaVersionId);
        for (InputControlPojo o : oldControls) {
            InputControlPojo p = getInputControlPojoFromOldControl(newSchemaVersionId, o);
            InputControlQueryPojo q = null;
            List<InputControlValuesPojo> v = new ArrayList<>();
            InputControlQueryPojo oldQuery = inputControlApi.selectControlQuery(o.getId());
            if (Objects.nonNull(oldQuery)) {
                q = new InputControlQueryPojo();
                q.setQuery(oldQuery.getQuery());
            }
            List<InputControlValuesPojo> oldValues =
                    inputControlApi.selectControlValues(Collections.singletonList(o.getId()));
            if (!oldValues.isEmpty()) {
                oldValues.forEach(ov -> {
                    InputControlValuesPojo valuesPojo = new InputControlValuesPojo();
                    valuesPojo.setValue(ov.getValue());
                    v.add(valuesPojo);
                });
            }
            inputControlApi.add(p, q, v);
            oldToNewControls.put(o.getId(), p.getId());
        }
        return oldToNewControls;
    }
}
