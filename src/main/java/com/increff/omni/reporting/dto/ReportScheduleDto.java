package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ReportScheduleApi;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.increff.omni.reporting.dto.CommonDtoHelper.convertFormToReportSchedulePojo;

@Component
public class ReportScheduleDto extends AbstractDto {

    @Autowired
    private ReportScheduleApi api;

    public void scheduleReport(ReportScheduleForm form) throws ApiException {
        checkValid(form);
        List<ReportSchedulePojo> reportSchedulePojoList = api.selectByOrgId(getOrgId());
        // todo write logic to limit number of schedules for organization
        ReportSchedulePojo pojo = convertFormToReportSchedulePojo(form, getOrgId(), getUserId());
        api.add(pojo);
    }

}
