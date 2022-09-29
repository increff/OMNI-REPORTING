package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.ReportRequestPojo;

import static com.increff.omni.reporting.dto.AbstractDto.getOrgId;
import static com.increff.omni.reporting.dto.AbstractDto.getUserId;

public class ReportRequestDtoHelper {

    public static ReportRequestPojo getReportRequestPojo(ReportRequestForm form) {
        ReportRequestPojo reportRequestPojo = new ReportRequestPojo();
        reportRequestPojo.setReportId(form.getReportId());
        reportRequestPojo.setOrgId(getOrgId());
        reportRequestPojo.setStatus(ReportRequestStatus.NEW);
        reportRequestPojo.setUserId(getUserId());
        return reportRequestPojo;
    }
}
