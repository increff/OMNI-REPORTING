package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.api.ReportValidationGroupApi;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.data.ValidationGroupData;
import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.omni.reporting.model.form.ReportQueryForm;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportDto extends AbstractDtoApi {

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

    public ReportData add(ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo = flowApi.addReport(pojo);
        return ConvertUtil.convert(pojo, ReportData.class);
    }

    public ReportData edit(Integer id, ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo.setId(id);
        pojo = flowApi.editReport(pojo);
        return ConvertUtil.convert(pojo, ReportData.class);
    }

    public ReportData get(Integer id) throws ApiException {
        ReportPojo pojo = reportApi.getCheck(id);
        return ConvertUtil.convert(pojo, ReportData.class);
    }

    public ReportQueryData upsertQuery(Integer reportId, ReportQueryForm form) throws ApiException {
        checkValid(form);
        ReportQueryPojo pojo = ConvertUtil.convert(form, ReportQueryPojo.class);
        pojo.setReportId(reportId);
        pojo = flowApi.upsertQuery(pojo);
        return ConvertUtil.convert(pojo, ReportQueryData.class);
    }


    public List<ReportData> selectAll(Integer orgId) throws ApiException {
        List<ReportPojo> pojos = flowApi.getAll(orgId);
        return ConvertUtil.convert(pojos, ReportData.class);
    }

    public void mapToControl(Integer reportId, Integer controlId) throws ApiException {
        if (reportId == null || controlId == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report id or control id cannot be null");
        ReportControlsPojo pojo = CommonDtoHelper.getReportControlPojo(reportId, controlId);
        flowApi.mapControlToReport(pojo);
    }

    public void deleteReportControl(Integer reportId, Integer reportControlId) throws ApiException {
        flowApi.deleteReportControl(reportId, reportControlId);
    }

    public void addValidationGroup(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        validate(reportId, groupForm);
        flowApi.addValidationGroup(reportId, groupForm);
    }

    public void deleteValidationGroup(Integer reportId, String groupName) throws ApiException {
        reportValidationGroupApi.deleteByReportIdAndGroupName(reportId, groupName);
    }

    public List<ValidationGroupData> getValidationGroups(Integer reportId) {
        List<ValidationGroupData> validationGroupDataList = new ArrayList<>();
        List<ReportValidationGroupPojo> validationGroupPojoList = reportValidationGroupApi.getByReportId(reportId);
        validationGroupPojoList.forEach(v -> {
            ValidationGroupData data = new ValidationGroupData();
            List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByIds(validationGroupPojoList.stream()
                    .map(ReportValidationGroupPojo::getReportControlId).collect(Collectors.toList()));
            List<Integer> controlIds = reportControlsPojoList.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
            List<InputControlPojo> pojos = inputControlApi.selectMultiple(controlIds);
            data.setValidationValue(v.getValidationValue());
            data.setGroupName(v.getGroupName());
            data.setValidationType(v.getType());
            data.setControls(pojos.stream().map(InputControlPojo::getDisplayName).collect(Collectors.toList()));
            validationGroupDataList.add(data);
        });
        return validationGroupDataList;
    }

    private void validate(Integer reportId, ValidationGroupForm groupForm) throws ApiException {
        checkValid(groupForm);
        if (Objects.isNull(reportId))
            throw new ApiException(ApiStatus.BAD_DATA, "Report id cannot be null");
        if (groupForm.getReportControlIds().stream().distinct().count() != groupForm.getReportControlIds().size())
            throw new ApiException(ApiStatus.BAD_DATA, "Validation group contains duplicate report control ids");
        if (groupForm.getValidationType().equals(ValidationType.DATE_RANGE) && groupForm.getValidationValue() <= 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Date range validation should have positive validation value");
    }
}
