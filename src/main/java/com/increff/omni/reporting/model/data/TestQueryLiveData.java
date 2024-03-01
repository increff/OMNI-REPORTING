package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TestQueryLiveData {
    private Integer testedOrgId;
    private Integer testedSchemaVersionId;
    private Integer testedConnectionId;
    private List<ViewDashboardData> viewDashboardData;
}
