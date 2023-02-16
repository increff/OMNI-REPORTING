package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.form.CronScheduleForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Getter
@Setter
public class ReportScheduleData {

    private String timezone;
    private Integer reportId;
    private String reportName;
    private ReportRequestType type;
    private String sendTo;
    private Boolean isEnabled;
    private ZonedDateTime nextRuntime;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

}
