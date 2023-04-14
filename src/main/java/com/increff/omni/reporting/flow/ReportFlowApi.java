package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.*;

@Service
@Setter
@Transactional(rollbackFor = ApiException.class)
@Log4j
public class ReportFlowApi extends AbstractFlowApi {

    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
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
    private InputControlApi inputControlApi;
    @Autowired
    private ApplicationProperties properties;

    private static final Integer MAX_NUMBER_OF_ROWS = 300;


    public ReportPojo addReport(ReportPojo pojo) throws ApiException {
        validate(pojo);
        return api.add(pojo);
    }

    public ReportPojo editReport(ReportPojo pojo) throws ApiException {
        ReportPojo existing = api.getCheck(pojo.getId());
        validateForEdit(pojo);
        // Delete custom report access if transition is happening from CUSTOM to STANDARD
        if (existing.getType().equals(ReportType.CUSTOM) && pojo.getType().equals(ReportType.STANDARD))
            customReportAccessApi.deleteByReportId(pojo.getId());
        return api.edit(pojo);
    }

    public List<Map<String, String>> validateAndGetLiveData(ReportPojo reportPojo, Integer orgId,
                                                            List<ReportInputParamsPojo> reportInputParamsPojoList)
            throws ApiException, IOException {
        validate(reportPojo, reportInputParamsPojoList);
        ReportQueryPojo reportQueryPojo = queryApi.getByReportId(reportPojo.getId());
        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(orgId);
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
        File file = folderApi.getFileForExtension(reportPojo.getId(), ".tsv");
        File errorFile = folderApi.getErrFile(reportPojo.getId(), ".tsv");;
        // Creation of file
        try {
            Map<String, String> inputParamMap = getInputParamMapFromPojoList(reportInputParamsPojoList);
            SqlParams sqlParams = CommonDtoHelper.convert(connectionPojo, file, errorFile);
            String fQuery = SqlCmd.prepareQuery(inputParamMap, reportQueryPojo.getQuery(),
                    properties.getLiveReportMaxExecutionTime());
            sqlParams.setQuery(fQuery);
            // Execute query and save results
            SqlCmd.processQuery(sqlParams, true, properties.getLiveReportMaxExecutionTime());
            if (FileUtil.getNumberOfRows(sqlParams.getOutFile()) > MAX_NUMBER_OF_ROWS) {
                throw new ApiException(ApiStatus.BAD_DATA, "Data exceeded " + MAX_NUMBER_OF_ROWS + " Rows, select " +
                        "granular filters.");
            }
            return FileUtil.getJsonDataFromFile(file, '\t');
        } catch (Throwable e) {
            log.info("Failed to get the data for dashboard for report id : " + reportPojo.getId() + " , org id : " + orgId,
                    e);
            throw new ApiException(ApiStatus.BAD_DATA, "Failed to get the data for dashboard." + e.getMessage());
        } finally {
            deleteFiles(file, errorFile);
        }
    }

    public ReportQueryPojo upsertQuery(ReportQueryPojo pojo) throws ApiException {
        api.getCheck(pojo.getReportId());
        return queryApi.upsertQuery(pojo);
    }

    public void mapControlToReport(ReportControlsPojo pojo) throws ApiException {
        validateControlReportMapping(pojo);
        reportControlsApi.add(pojo);
        checkAndAddValidationGroup(pojo);
    }

    public List<ReportPojo> getAllBySchemaVersionId(Integer schemaVersionId) {
        return api.getBySchemaVersion(schemaVersionId);
    }

    public List<ReportPojo> getAll(Integer orgId, Boolean isDashboard) throws ApiException {

        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(orgId);
        //All standard
        List<ReportPojo> standard =
                api.getByTypeAndSchema(ReportType.STANDARD, orgSchemaVersionPojo.getSchemaVersionId(), isDashboard);

        //All custom
        List<CustomReportAccessPojo> customAccess = customReportAccessApi.getByOrgId(orgId);
        List<Integer> customIds = customAccess.stream().map(CustomReportAccessPojo::getReportId)
                .collect(Collectors.toList());

        List<ReportPojo> custom =
                api.getByIdsAndSchema(customIds, orgSchemaVersionPojo.getSchemaVersionId(), isDashboard);

        //combined 2 list
        standard.addAll(custom);
        return standard;
    }

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

    public void deleteReportControl(Integer reportId, Integer controlId) throws ApiException {
        api.getCheck(reportId);
        ReportControlsPojo pojo = reportControlsApi.getByReportAndControlId(reportId, controlId);
        checkNotNull(pojo, "No report control exist with control id : " + controlId);
        reportValidationGroupApi.deleteByReportIdAndReportControlId(reportId, pojo.getId());
        reportControlsApi.delete(pojo.getId());
    }

    public void copyReports(Integer oldSchemaVersionId, Integer newSchemaVersionId) throws ApiException {
        schemaVersionApi.getCheck(oldSchemaVersionId);
        schemaVersionApi.getCheck(newSchemaVersionId);
        // Migrate Input controls
        Map<Integer, Integer> oldToNewControlIds = migrateInputControls(oldSchemaVersionId, newSchemaVersionId);
        // Migrate Reports
        List<ReportPojo> oldSchemaReports = api.getBySchemaVersion(oldSchemaVersionId);
        for (ReportPojo oldReport : oldSchemaReports) {
            ReportPojo ex = api.getByNameAndSchema(oldReport.getName(), newSchemaVersionId, oldReport.getIsDashboard());
            if (Objects.nonNull(ex))
                continue;
            // Add Report
            ReportPojo pojo = getReportPojoFromExistingPojo(oldReport);
            pojo.setSchemaVersionId(newSchemaVersionId);
            addReport(pojo);
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

    private void validateForEdit(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        // validating if requested name is already present
        ReportPojo existingByName =
                api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId(), pojo.getIsDashboard());
        if (Objects.nonNull(existingByName) && !existingByName.getId().equals(pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name, schema version and " +
                    "report type (normal / dashboard)");
    }

    private void validate(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        ReportPojo existing = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId(), pojo.getIsDashboard());
        if (existing != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name, schema version and " +
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

    private void deleteFiles(File outFile, File errFile) {
        if (outFile.exists() && !FileUtil.delete(outFile))
            log.debug("File deletion failed, name : " + outFile.getName());
        if (errFile.exists() && !FileUtil.delete(errFile))
            log.debug("File deletion failed, name : " + errFile.getName());
    }
}
