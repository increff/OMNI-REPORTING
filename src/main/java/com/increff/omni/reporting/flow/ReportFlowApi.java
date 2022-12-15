package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getReportPojoFromExistingPojo;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getValidationGroupPojoFromExistingPojo;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportFlowApi extends AbstractAuditApi {

    @Autowired
    private SchemaVersionApi schemaVersionApi;

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

    public List<ReportPojo> getAll(Integer orgId) throws ApiException {

        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(orgId);
        //All standard
        List<ReportPojo> standard =
                api.getByTypeAndSchema(ReportType.STANDARD, orgSchemaVersionPojo.getSchemaVersionId());

        //All custom
        List<CustomReportAccessPojo> customAccess = customReportAccessApi.getByOrgId(orgId);
        List<Integer> customIds = customAccess.stream().map(CustomReportAccessPojo::getReportId)
                .collect(Collectors.toList());

        List<ReportPojo> custom = api.getByIdsAndSchema(customIds, orgSchemaVersionPojo.getSchemaVersionId());

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
        List<ReportPojo> oldSchemaReports = api.getBySchemaVersion(oldSchemaVersionId);
        for (ReportPojo oldReport : oldSchemaReports) {
            ReportPojo ex = api.getByNameAndSchema(oldReport.getName(), newSchemaVersionId);
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
                p.setControlId(c.getControlId());
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
        if (Arrays.asList(InputControlType.MULTI_SELECT, InputControlType.SINGLE_SELECT)
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
        api.getCheck(pojo.getReportId());
        //valid control - Global
        InputControlPojo icPojo = inputControlApi.getCheck(pojo.getControlId());
        if (icPojo.getScope().equals(InputControlScope.LOCAL))
            throw new ApiException(ApiStatus.BAD_DATA, "Only Global Control can be mapped to a report");
    }

    private void validateForEdit(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        // validating if requested name is already present
        ReportPojo existingByName = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId());
        if (Objects.nonNull(existingByName) && !existingByName.getId().equals(pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name and schema version");
    }

    private void validate(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaVersionApi.getCheck(pojo.getSchemaVersionId());

        ReportPojo existing = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId());
        if (existing != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name and schema version");

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
}
