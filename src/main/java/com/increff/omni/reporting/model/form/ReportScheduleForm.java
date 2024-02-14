package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReportScheduleForm {

    @NotNull
    private String timezone = "UTC";
    @NotNull
    private String reportAlias;
    private List<InputParamMap> paramMap;
    private CronScheduleForm cronSchedule;
    private List<String> sendTo = new ArrayList<>();
    private List<PipelineDetailsForm> pipelineDetails = new ArrayList<>();

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
