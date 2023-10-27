package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronScheduleForm {

    private String dayOfMonth;
    private String dayOfWeek;
    private String hour;
    private String minute;
    private Boolean isWeeklySchedule; // needed in response data to set form field values in UI

}
