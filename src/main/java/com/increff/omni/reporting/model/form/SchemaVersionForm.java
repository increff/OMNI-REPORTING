package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.AppName;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class SchemaVersionForm {

    @NotEmpty
    private String name;
    @NotNull
    private AppName appName;
}
