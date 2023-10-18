package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronScheduleForm {

    private String dayOfMonth;
    private String hour;
    private String minute;
    private String dayOfWeek;

}
