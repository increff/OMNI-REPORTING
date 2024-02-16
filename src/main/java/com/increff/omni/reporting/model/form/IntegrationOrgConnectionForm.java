package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.AppName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntegrationOrgConnectionForm {
    private String orgName;
    private String connectionName;
    private AppName appName;
}
