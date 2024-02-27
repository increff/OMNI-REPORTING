package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestQueryLiveData {
    private Integer testedOrgId;
    private Integer testedSchemaVersionId;
    private Integer testedConnectionId;
    private ViewDashboardData viewDashboardData;
}
