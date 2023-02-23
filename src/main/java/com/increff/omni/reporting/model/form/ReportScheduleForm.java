package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class ReportScheduleForm {

    @NotNull
    private String timezone;
    @NotNull
    private String reportName;
    private CronScheduleForm cronSchedule;
    @NotNull
    private List<String> sendTo;
    @NotNull
    private Boolean isEnabled;

}
