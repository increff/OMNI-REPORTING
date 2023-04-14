package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.CronScheduleForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class ReportScheduleData {

    private Integer id;
    private String timezone;
    private String reportName;
    private Boolean isEnabled;
    private CronScheduleForm cronSchedule;
    private List<InputControlFilterData> filters;
    private List<String> sendTo;
    private ZonedDateTime nextRuntime;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Integer successCount;
    private Integer failureCount;


}
