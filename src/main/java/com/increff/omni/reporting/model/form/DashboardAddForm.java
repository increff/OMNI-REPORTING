package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Setter
@Getter
public class DashboardAddForm {
    @NotNull
    private String name;
    @NotNull
    List<DashboardChartForm> charts;
}
