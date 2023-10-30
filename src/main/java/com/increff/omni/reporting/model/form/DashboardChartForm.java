package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class DashboardChartForm {
    @NotNull
    private String chartAlias;
    @NotNull
    private Integer row;
    @NotNull
    private Integer col;
    @NotNull
    private Integer colWidth;
}
