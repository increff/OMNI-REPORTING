package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.RowHeight;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class DashboardChartForm {
    @NotNull
    private String chartAlias;
    @NotNull
    @Min(0)
    private Integer row;
    @NotNull
    @Min(0)
    private Integer col;
    @NotNull
    @Min(0)
    @Max(12)
    private Integer colWidth;
    @NotNull
    private RowHeight rowHeight;
}
