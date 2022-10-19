package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getValidationGroupPojoList;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportFlowApi extends AbstractApi {

    @Autowired
    private SchemaApi schemaApi;

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
        validateForEdit(pojo, existing);
        // Delete custom report access if transition is happening from CUSTOM to STANDARD
        if (existing.getType().equals(ReportType.CUSTOM) && pojo.getType().equals(ReportType.STANDARD))
            customReportAccessApi.deleteByReportId(pojo.getId());
        return api.edit(pojo);
    }

    public ReportQueryPojo upsertQuery(ReportQueryPojo pojo) throws ApiException {
        api.getCheck(pojo.getReportId());
        return queryApi.upsertQuery(pojo);
    }

    public void deleteReportControl(Integer reportId, Integer reportControlId) throws ApiException {
        api.getCheck(reportId);
        reportControlsApi.getCheck(reportControlId);
        List<ReportValidationGroupPojo> validationGroupPojoList = reportValidationGroupApi
                .getByReportIdAndReportControlId(reportId, reportControlId);
        reportValidationGroupApi.delete(validationGroupPojoList);
        reportControlsApi.delete(reportControlId);
    }

    public void mapControlToReport(ReportControlsPojo pojo) throws ApiException {
        validateControlReportMapping(pojo);
        reportControlsApi.add(pojo);
    }

    public List<ReportPojo> getAll(Integer orgId) throws ApiException {

        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(orgId);
        //All standard
        List<ReportPojo> standard = api.getByTypeAndSchema(ReportType.STANDARD, orgSchemaVersionPojo.getSchemaVersionId());

        //All custom
        List<CustomReportAccessPojo> customAccess = customReportAccessApi.getByOrgId(orgId);
        List<Integer> customIds = customAccess.stream().map(CustomReportAccessPojo::getReportId).collect(Collectors.toList());

        List<ReportPojo> custom = api.getByIdsAndSchema(customIds, orgSchemaVersionPojo.getSchemaVersionId());

        //combined 2 list
        standard.addAll(custom);
        return standard;
    }

    public void addValidationGroup(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        validate(reportId, groupForm);
        List<InputControlPojo> inputControlPojoList = inputControlApi.selectMultiple(groupForm.getReportControlIds());
        List<InputControlType> controlTypeList = inputControlPojoList.stream().map(InputControlPojo::getType)
                .collect(Collectors.toList());
        switch (groupForm.getValidationType()) {
            case NON_MANDATORY:
                break;
            case MANDATORY:
                mandatoryValidator.add(controlTypeList);
                break;
            case DATE_RANGE:
                dateValidator.add(controlTypeList);
                break;
            case SINGLE_MANDATORY:
                singleMandatoryValidator.add(controlTypeList);
                break;
        }
        List<ReportValidationGroupPojo> validationGroupPojoList = getValidationGroupPojoList(groupForm, reportId);
        reportValidationGroupApi.addAll(validationGroupPojoList);
    }

    private void validate(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        api.getCheck(reportId);
        for (Integer reportControlId : groupForm.getReportControlIds())
            reportControlsApi.getCheck(reportControlId);
        ReportValidationGroupPojo pojo = reportValidationGroupApi.getByNameAndReportId(reportId, groupForm.getGroupName());
        if (Objects.nonNull(pojo))
            throw new ApiException(ApiStatus.BAD_DATA, "Group name already exist for given report, group name : " + groupForm.getGroupName());
    }

    private void validateControlReportMapping(ReportControlsPojo pojo) throws ApiException {
        //valid report
        api.getCheck(pojo.getReportId());
        //valid control - Global
        InputControlPojo icPojo = inputControlApi.getCheck(pojo.getControlId());
        if (icPojo.getScope().equals(InputControlScope.LOCAL))
            throw new ApiException(ApiStatus.BAD_DATA, "Only Global Control can be mapped to a report");
    }

    private void validateForEdit(ReportPojo pojo, ReportPojo existing) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaApi.getCheck(pojo.getSchemaVersionId());

        //validating if requested name is already present
        if (!pojo.getName().equals(existing.getName())) {
            ReportPojo existingByName = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId());
            if (existingByName != null)
                throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name");
        }
    }

    private void validate(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaApi.getCheck(pojo.getSchemaVersionId());

        //get all
        ReportPojo existing = api.getByNameAndSchema(pojo.getName(), pojo.getSchemaVersionId());
        if (existing != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name");

    }
}
