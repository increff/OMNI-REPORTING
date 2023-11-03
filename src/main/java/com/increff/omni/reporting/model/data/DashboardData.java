package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.DashboardForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DashboardData {

    private Integer id;
    private DashboardForm dashboardDetails;
    private List<InputControlData> filterDetails;
    private List<List<DashboardGridData>> grid;
}
