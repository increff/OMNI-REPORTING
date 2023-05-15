package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.ReportScheduleFlowApi;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.data.InputControlFilterData;
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

import static com.increff.omni.reporting.dto.CommonDtoHelper.*;

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
    private ReportQueryApi reportQueryApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;

    public void scheduleReport(ReportScheduleForm form) throws ApiException {
        checkValid(form);
        checkLimitForOrg();
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        ReportPojo reportPojo = checkValidReport(form.getReportName());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojos =
                validateAndPrepareInputParams(form, reportPojo);
        flowApi.add(pojo, form.getSendTo(), reportScheduleInputParamsPojos, reportPojo);
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
        ReportPojo reportPojo = checkValidReport(form.getReportName());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        pojo.setId(id);
        List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojos =
                validateAndPrepareInputParams(form, reportPojo);
        flowApi.edit(pojo, form.getSendTo(), reportScheduleInputParamsPojos, reportPojo);
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
        api.edit(pojo);
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.ENABLE_DISABLE_REPORT_SCHEDULE.toString(),
                "Enable / Disable " +
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
        api.edit(pojo);
        flowApi.saveAudit(pojo.getId().toString(), AuditActions.DELETE_REPORT_SCHEDULE.toString(), "Delete Report " +
                        "Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with status isDeleted : " + pojo.getIsDeleted(), getUserName());

    }

    public List<ReportScheduleData> getScheduleReportsForAllOrgs(Integer pageNo, Integer pageSize) throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList =
                api.selectByOrgIdAndEnabledStatus(null, null, pageNo, pageSize);
        return getReportScheduleData(reportSchedulePojoList);
    }

    public List<ReportScheduleData> getScheduleReports(Integer pageNo, Integer pageSize) throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgIdAndEnabledStatus(getOrgId(), null, pageNo,
                pageSize);
        return getReportScheduleData(reportSchedulePojoList);
    }

    public List<ReportRequestData> getScheduledRequests(Integer pageNo, Integer pageSize) throws ApiException {
        List<ReportRequestData> reportRequestDataList = new ArrayList<>();
        List<ReportRequestPojo> reportRequestPojoList =
                reportRequestApi.getByOrgAndType(getOrgId(), ReportRequestType.EMAIL
                        , pageNo, pageSize);
        for (ReportRequestPojo r : reportRequestPojoList) {
            ReportPojo reportPojo = Objects.isNull(r.getReportId()) ? null : reportApi.getCheck(r.getReportId());
            List<ReportControlsPojo> reportControlsPojos = Objects.isNull(r.getReportId()) ?
                    new ArrayList<>() : reportControlsApi.getByReportId(r.getReportId());
            List<Integer> controlIds = reportControlsPojos.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
            List<InputControlPojo> controlPojos = controlIds.isEmpty() ? new ArrayList<>() :
                    controlApi.selectByIds(controlIds);
            List<ReportInputParamsPojo> paramsPojoList = reportInputParamsApi.getInputParamsForReportRequest(r.getId());
            OrganizationPojo organizationPojo = organizationApi.getCheck(r.getOrgId());
            reportRequestDataList.add(
                    getReportRequestData(r, reportPojo, controlPojos, paramsPojoList, organizationPojo, 0));
        }
        return reportRequestDataList;
    }

    private List<ReportScheduleData> getReportScheduleData(List<ReportSchedulePojo> reportSchedulePojoList)
            throws ApiException {
        List<ReportScheduleData> dataList = new ArrayList<>();
        for (ReportSchedulePojo pojo : reportSchedulePojoList) {
            OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(pojo.getOrgId());
            ReportPojo reportPojo = reportApi.getByNameAndSchema(pojo.getReportName(),
                    orgSchemaVersionPojo.getSchemaVersionId(), false);
            List<ReportControlsPojo> reportControlsPojos = Objects.isNull(reportPojo) ?
                    new ArrayList<>() : reportControlsApi.getByReportId(reportPojo.getId());
            List<Integer> controlIds = reportControlsPojos.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList());
            List<InputControlPojo> controlPojos = controlIds.isEmpty() ? new ArrayList<>() :
                    controlApi.selectByIds(controlIds);
            List<ReportScheduleEmailsPojo> emailsPojos = api.getByScheduleId(pojo.getId());
            List<ReportScheduleInputParamsPojo> paramsPojos = api.getScheduleParams(pojo.getId());
            String timezone = paramsPojos.stream().filter(p -> p.getParamKey().equalsIgnoreCase("timezone")).collect(
                    Collectors.toList()).get(0).getParamValue();
            List<InputControlFilterData> filterData = prepareFilters(paramsPojos, controlPojos);
            ReportScheduleData data = ConvertUtil.convert(pojo, ReportScheduleData.class);
            data.setFilters(filterData);
            CronScheduleForm cronData = new CronScheduleForm();
            cronData.setDayOfMonth(pojo.getCron().split(" ")[3]);
            cronData.setHour(pojo.getCron().split(" ")[2]);
            cronData.setMinute(pojo.getCron().split(" ")[1]);
            data.setCronSchedule(cronData);
            data.setTimezone(getValueFromQuotes(timezone));
            data.setSendTo(emailsPojos.stream().map(ReportScheduleEmailsPojo::getSendTo).collect(Collectors.toList()));
            dataList.add(data);
        }
        return dataList;
    }

    private ReportPojo checkValidReport(String reportName) throws ApiException {
        OrgSchemaVersionPojo orgSchemaVersionPojo = orgSchemaApi.getCheckByOrgId(getOrgId());
        ReportPojo reportPojo =
                reportApi.getByNameAndSchema(reportName, orgSchemaVersionPojo.getSchemaVersionId(), false);
        if (Objects.isNull(reportPojo) || !reportPojo.getCanSchedule())
            throw new ApiException(ApiStatus.BAD_DATA, "Report : " + reportName + " is not allowed to " +
                    "schedule");
        ReportQueryPojo reportQueryPojo = reportQueryApi.getByReportId(reportPojo.getId());
        if (Objects.isNull(reportQueryPojo))
            throw new ApiException(ApiStatus.BAD_DATA, "Report : " + reportName + " doesn't have any query defined.");
        return reportPojo;
    }

    private void checkLimitForOrg() throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList =
                api.selectByOrgIdAndEnabledStatus(getOrgId(), true, null, null);
        if (reportSchedulePojoList.size() >= properties.getMaxScheduleLimit())
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization has crossed max schedule limit of " + properties.getMaxScheduleLimit() + " " +
                            " enabled reports");
    }

    private List<ReportScheduleInputParamsPojo> validateAndPrepareInputParams(ReportScheduleForm form,
                                                                              ReportPojo reportPojo)
            throws ApiException {
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
        return CommonDtoHelper.getReportScheduleInputParamsPojoList(inputParamsMap, form.getTimezone(), getOrgId(),
                inputDisplayStringMap);
    }

    private Map<String, List<String>> getUserInputParams(List<ReportScheduleForm.InputParamMap> paramMap) {
        Map<String, List<String>> inputParams = new HashMap<>();
        paramMap.forEach(p -> inputParams.put(p.getKey(), p.getValue()));
        return inputParams;
    }
}
