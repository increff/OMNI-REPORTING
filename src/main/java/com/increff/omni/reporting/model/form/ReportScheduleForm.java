package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportScheduleForm {

    private String timezone;
    private Integer reportId;
    private CronScheduleForm cronSchedule;
    private ReportRequestType type;
    private String sendTo;

}
