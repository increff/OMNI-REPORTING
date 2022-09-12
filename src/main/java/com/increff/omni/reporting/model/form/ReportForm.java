package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ReportForm {

    @NotNull
    private String name;
    @NotNull
    private ReportType type;
    @NotNull
    private Integer directoryId;
    @NotNull
    private Integer schemaId;

}
