package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.ReportScheduleFlowApi;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.ReportScheduleData;
import com.increff.omni.reporting.model.form.CronScheduleForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.convertFormToReportSchedulePojo;

@Component
public class ReportScheduleDto extends AbstractDto {

    @Autowired
    private ReportScheduleApi api;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private ReportRequestApi reportRequestApi;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private ReportScheduleFlowApi flowApi;
    @Autowired
    private ApplicationProperties properties;

    public void scheduleReport(ReportScheduleForm form) throws ApiException {
        checkValid(form);
        checkLimitForOrg();
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(getOrgId());

        ReportPojo reportPojo = checkValidReport(form.getReportName());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByReportId(reportPojo.getId());
        List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
        Map<String, List<String>> userInputParams = getUserInputParams(form.getParamMap());
        Map<String, String> inputParamsMap = UserPrincipalUtil.getCompleteMapWithAccessControl(form.getParamMap());
        Map<String, List<String>> inputDisplayMap = new HashMap<>();
        validateCustomReportAccess(reportPojo, getOrgId());
        validateInputParamValues(userInputParams, inputParamsMap, getOrgId(), inputDisplayMap, inputControlPojoList,
                ReportRequestType.EMAIL);
        Map<String, String> inputDisplayStringMap = UserPrincipalUtil.getStringToStringParamMap(inputDisplayMap);
        List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojos =
                CommonDtoHelper.getReportScheduleInputParamsPojoList(inputParamsMap, form.getTimezone(), getOrgId(),
                        inputDisplayStringMap);
        flowApi.add(pojo, form.getSendTo(), reportScheduleInputParamsPojos);
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.CREATE_REPORT_SCHEDULE.toString(),
                "Create Report Schedule",
                "Report schedule created for organization : " + organizationPojo.getName(), getUserName());
    }

    public void editScheduleReport(Integer id, ReportScheduleForm form) throws ApiException {
        checkValid(form);
        checkLimitForOrg();
        ReportSchedulePojo ex = api.getCheck(id);
        if (!ex.getOrgId().equals(getOrgId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Org ID mismatch, existing org id : " + ex.getOrgId() + " , " +
                    "new org id : " + getOrgId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        checkValidReport(form.getReportName());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        pojo.setId(id);
        flowApi.edit(pojo, form.getSendTo());
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.EDIT_REPORT_SCHEDULE.toString(), "Edit Report Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with cron : " + pojo.getCron(), getUserName());
    }

    public void updateStatus(Integer id, Boolean isEnabled) throws ApiException {
        if (isEnabled)
            checkLimitForOrg();
        ReportSchedulePojo pojo = api.getCheck(id);
        if (!pojo.getOrgId().equals(getOrgId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Org ID mismatch, existing org id : " + pojo.getOrgId() + " , " +
                    "new org id : " + getOrgId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        checkValidReport(pojo.getReportName());
        pojo.setIsEnabled(isEnabled);
        flowApi.editEnableOrDeletedFlag(pojo);
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.ENABLE_DISABLE_REPORT_SCHEDULE.toString(), "Enable / Disable " +
                        "Report Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with status isEnabled : " + pojo.getIsEnabled(), getUserName());
    }

    public void deleteSchedule(Integer id) throws ApiException {
        ReportSchedulePojo pojo = api.getCheck(id);
        if (!pojo.getOrgId().equals(getOrgId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Org ID mismatch, existing org id : " + pojo.getOrgId() + " , " +
                    "new org id : " + getOrgId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        pojo.setIsDeleted(true);
        flowApi.editEnableOrDeletedFlag(pojo);
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.DELETE_REPORT_SCHEDULE.toString(), "Delete Report " +
                        "Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with status isDeleted : " + pojo.getIsDeleted(), getUserName());

    }

    public List<ReportScheduleData> getScheduleReportsForAllOrgs(Integer pageNo, Integer pageSize) throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgIdAndEnabledStatus(null, null, pageNo, pageSize);
        return getReportScheduleData(reportSchedulePojoList);
    }

    public List<ReportScheduleData> getScheduleReports(Integer pageNo, Integer pageSize) throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgIdAndEnabledStatus(getOrgId(), null, pageNo,
                pageSize);
        return getReportScheduleData(reportSchedulePojoList);
    }

    // todo
    public List<ReportRequestData> getScheduledRequests() throws ApiException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList = reportRequestApi.getByOrg(getOrgId(), ReportRequestType.EMAIL);
        for (ReportRequestPojo r : reportRequestPojoList) {
            ReportPojo reportPojo = reportApi.getCheck(r.getReportId());
            reportRequestDataList.add(getReportRequestData(r, reportPojo));
        }
        return reportRequestDataList;
    }

    private List<ReportScheduleData> getReportScheduleData(List<ReportSchedulePojo> reportSchedulePojoList) {
        List<ReportScheduleData> dataList = new ArrayList<>();
        for (ReportSchedulePojo pojo : reportSchedulePojoList) {
            List<ReportScheduleEmailsPojo> emailsPojos = api.getByScheduleId(pojo.getId());
            ReportScheduleData data = ConvertUtil.convert(pojo, ReportScheduleData.class);
            CronScheduleForm cronData = new CronScheduleForm();
            cronData.setDayOfMonth(pojo.getCron().split(" ")[3]);
            cronData.setHour(pojo.getCron().split(" ")[2]);
            cronData.setMinute(pojo.getCron().split(" ")[1]);
            data.setCronSchedule(cronData);
            data.setSendTo(emailsPojos.stream().map(ReportScheduleEmailsPojo::getSendTo).collect(Collectors.toList()));
            dataList.add(data);
        }
        return dataList;
    }

    private ReportRequestData getReportRequestData(ReportRequestPojo pojo, ReportPojo reportPojo) throws ApiException {
        OrganizationPojo organizationPojo = organizationApi.getCheck(pojo.getOrgId());
        ReportRequestData data = new ReportRequestData();
        data.setRequestCreationTime(pojo.getCreatedAt());
        data.setRequestUpdatedTime(pojo.getUpdatedAt());
        data.setStatus(pojo.getStatus());
        data.setType(pojo.getType());
        data.setRequestId(pojo.getId());
        data.setReportId(reportPojo.getId());
        data.setReportName(reportPojo.getName());
        data.setOrgName(organizationPojo.getName());
        data.setFileSize(pojo.getFileSize());
        data.setNoOfRows(pojo.getNoOfRows());
        data.setFailureReason(pojo.getFailureReason());
        return data;
    }

    private ReportPojo checkValidReport(String reportName) throws ApiException {
        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(getOrgId());
        ReportPojo reportPojo = reportApi.getByNameAndSchema(reportName, orgSchemaVersionPojo.getSchemaVersionId());
        if (Objects.isNull(reportPojo) || !reportPojo.getCanSchedule())
            throw new ApiException(ApiStatus.BAD_DATA, "Report : " + reportName + " is not allowed to " +
                    "schedule");
        return reportPojo;
    }

    private void checkLimitForOrg() throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgIdAndEnabledStatus(getOrgId(), true, null, null);
        if (reportSchedulePojoList.size() >= properties.getMaxScheduleLimit())
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization has crossed max schedule limit of " + properties.getMaxScheduleLimit() + " " +
                            " enabled reports");
    }

    private Map<String, List<String>> getUserInputParams(List<ReportScheduleForm.InputParamMap> paramMap) {
        Map<String, List<String>> inputParams = new HashMap<>();
        paramMap.forEach(p -> {
            inputParams.put(p.getKey(), p.getValue());
        });
        return inputParams;
    }
}
