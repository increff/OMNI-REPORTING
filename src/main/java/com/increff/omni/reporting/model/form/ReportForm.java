package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.constants.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
