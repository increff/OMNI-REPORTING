package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReportScheduleForm {

    @NotNull
    private String timezone = "UTC";
    @NotNull
    private String reportName;
    private List<InputParamMap> paramMap;
    private CronScheduleForm cronSchedule;
    @NotNull
    private List<String> sendTo;
    @NotNull
    private Boolean isEnabled;

    @Getter
    @Setter
    public static class InputParamMap {
        private String key;
        private List<String> value;
        private InputControlType type;
    }
}
