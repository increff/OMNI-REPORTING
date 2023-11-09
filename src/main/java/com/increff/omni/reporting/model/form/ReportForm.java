package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ReportForm {

    @NotEmpty
    private String name;
    @NotNull
    private ReportType type;
    @NotNull
    private Integer directoryId;
    @NotEmpty
    private String alias;

    private Integer schemaVersionId;
    @NotNull
    private Boolean isEnabled = true;
    @NotNull
    private Boolean canSchedule = false;
    @NotNull
    private Boolean isReport = false; // TODO: Rename to is Chart
    @NotNull
    private ChartType chartType;


}
