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
    private Integer controlId;
    @NotNull
    private List<String> defaultValue;
    // TODO: test how this will work with dynamic values getting from queries and how date will be handled
}
