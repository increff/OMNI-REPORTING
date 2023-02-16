package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.OrganizationApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportRequestApi;
import com.increff.omni.reporting.api.ReportScheduleApi;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.ReportScheduleData;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
    private ReportRequestApi reportRequestApi;

    public void scheduleReport(ReportScheduleForm form) throws ApiException {
        checkValid(form);
        checkLimitForOrg();
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        reportApi.getCheck(form.getReportId());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        api.add(pojo);
        api.saveAudit(pojo.getId().toString(), AuditActions.CREATE_REPORT_SCHEDULE.toString(), "Create Report Schedule",
                "Report schedule created for organization : " + organizationPojo.getName() , getUserName());
    }

    public void editScheduleReport(Integer id, ReportScheduleForm form) throws ApiException {
        checkValid(form);
        ReportSchedulePojo ex = api.getCheck(id);
        if(!ex.getOrgId().equals(getOrgId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Org ID mismatch, existing org id : " + ex.getOrgId() + " , " +
                    "new org id : " + getOrgId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        reportApi.getCheck(form.getReportId());
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        pojo.setId(id);
        api.edit(pojo);
        api.saveAudit(pojo.getId().toString(), AuditActions.EDIT_REPORT_SCHEDULE.toString(), "Edit Report Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with cron : " + pojo.getCron() , getUserName());
    }

    public void updateStatus(Integer id, Boolean isEnabled) throws ApiException {
        if(isEnabled)
            checkLimitForOrg();
        ReportSchedulePojo pojo = api.getCheck(id);
        if(!pojo.getOrgId().equals(getOrgId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Org ID mismatch, existing org id : " + pojo.getOrgId() + " , " +
                    "new org id : " + getOrgId());
        OrganizationPojo organizationPojo = organizationApi.getCheck(getOrgId());
        pojo.setIsEnabled(isEnabled);
        api.edit(pojo);
        api.saveAudit(pojo.getId().toString(), AuditActions.EDIT_REPORT_SCHEDULE.toString(), "Edit Report Schedule",
                "Report schedule updated for organization : " + organizationPojo.getName() +
                        " with status isEnabled : " + pojo.getIsEnabled() , getUserName());
    }

    public List<ReportScheduleData> getScheduleReports() throws ApiException {
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgId(getOrgId());
        List<ReportScheduleData> dataList = new ArrayList<>();
        for (ReportSchedulePojo pojo : reportSchedulePojoList) {
            ReportScheduleData data = ConvertUtil.convert(pojo, ReportScheduleData.class);
            ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
            data.setReportName(reportPojo.getName());
            dataList.add(data);
        }
        return dataList;
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

    private void checkLimitForOrg() {
        // todo
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgId(getOrgId());

    }
}
