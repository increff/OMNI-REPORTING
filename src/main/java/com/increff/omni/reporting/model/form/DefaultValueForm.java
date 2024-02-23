package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class DefaultValueForm {
    @NotNull
    private Integer dashboardId;
    @NotNull
    private Integer controlId;
    @NotNull
    private String chartAlias;
    @NotNull
    private List<String> defaultValue;
}
