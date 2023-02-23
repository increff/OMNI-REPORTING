package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.form.CronScheduleForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class ReportScheduleData {

    private String timezone;
    private String reportName;
    private Boolean isEnabled;
    private CronScheduleForm cronSchedule;
    private List<String> sendTo;
    private ZonedDateTime nextRuntime;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

}
