package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.flow.ReportRequestFlowApi;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.data.ValidationGroupData;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportDto extends AbstractDto {

    @Autowired
    private ReportFlowApi flowApi;
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

    public ReportData add(ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo = flowApi.addReport(pojo);
        return convertToReportData(Collections.singletonList(pojo)).get(0);
    }

    public ReportData edit(Integer id, ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo.setId(id);
        pojo = flowApi.editReport(pojo);
        return convertToReportData(Collections.singletonList(pojo)).get(0);
    }

    public ReportData get(Integer id) throws ApiException {
        ReportPojo pojo = reportApi.getCheck(id);
        return convertToReportData(Collections.singletonList(pojo)).get(0);
    }

    public ReportQueryData upsertQuery(Integer reportId, ReportQueryForm form) throws ApiException {
        checkValid(form);
        ReportQueryPojo pojo = ConvertUtil.convert(form, ReportQueryPojo.class);
        pojo.setReportId(reportId);
        pojo = flowApi.upsertQuery(pojo);
        return ConvertUtil.convert(pojo, ReportQueryData.class);
    }

    public ReportQueryData getTransformedQuery(ReportQueryTestForm form) {
        return new ReportQueryData();
    }

    public ReportQueryData getQuery(Integer reportId) throws ApiException {
        reportApi.getCheck(reportId);
        ReportQueryData data = new ReportQueryData();
        ReportQueryPojo queryPojo = reportQueryApi.getByReportId(reportId);
        data.setQuery(Objects.isNull(queryPojo) ? "" : queryPojo.getQuery());
        data.setUpdatedAt(Objects.isNull(queryPojo) ? null : queryPojo.getUpdatedAt());
        return data;
    }

    public List<ReportData> selectByOrg() throws ApiException {
        List<ReportPojo> pojos = flowApi.getAll(getOrgId());
        return convertToReportData(pojos);
    }

    public List<ReportData> selectAllBySchemaVersion(Integer schemaVersionId) throws ApiException {
        List<ReportPojo> pojos = flowApi.getAllBySchemaVersionId(schemaVersionId);
        return convertToReportData(pojos);
    }

    public void mapToControl(Integer reportId, Integer controlId) throws ApiException {
        if (reportId == null || controlId == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report id or control id cannot be null");
        ReportControlsPojo pojo = CommonDtoHelper.getReportControlPojo(reportId, controlId);
        flowApi.mapControlToReport(pojo);
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
            data.setGroupName(k);
            data.setValidationType(v.get(0).getType());
            data.setControls(pojos.stream().map(InputControlPojo::getDisplayName).collect(Collectors.toList()));
            validationGroupDataList.add(data);
        });
        return validationGroupDataList;
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
        for(ReportPojo p : pojos) {
            ReportData data = ConvertUtil.convert(p, ReportData.class);
            SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getCheck(data.getSchemaVersionId());
            DirectoryPojo directoryPojo = directoryApi.getCheck(data.getDirectoryId());
            data.setDirectoryName(directoryPojo.getDirectoryName());
            data.setDirectoryPath(directoryApi.getDirectoryPath(directoryPojo.getId()));
            data.setSchemaVersionName(schemaVersionPojo.getName());
            dataList.add(data);
        }
        return dataList;
    }
}
