package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
public class ApplicationPropertiesData {
    public Integer maxDashboardsPerOrg;
    public Integer maxChartsPerDashboard;
}
