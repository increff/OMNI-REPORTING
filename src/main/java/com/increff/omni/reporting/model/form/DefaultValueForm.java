package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class DefaultValueForm {
    @NotNull
    private Integer dashboardId;
    @NotNull
    private String paramName;
    @NotNull
    private List<String> defaultValue;
}
