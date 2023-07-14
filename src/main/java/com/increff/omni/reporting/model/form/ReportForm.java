package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class ReportForm {

    @NotEmpty
    private String name;
    @NotNull
    private ReportType type;
    @NotNull
    private Integer directoryId;

    private Integer schemaVersionId;
    @NotNull
    private Boolean isEnabled = true;
    @NotNull
    private Boolean canSchedule = false;
    @NotNull
    private Boolean isDashboard = false;


}
