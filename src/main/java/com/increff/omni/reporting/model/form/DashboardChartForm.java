package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
    @Min(0)
    @Max(12)
    private Integer colWidth;
}
