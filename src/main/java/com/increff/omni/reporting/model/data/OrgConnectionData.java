package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.AppName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrgConnectionData {

    private Integer orgId;
    private Integer connectionId;
    private String connectionName;
    private AppName appName;

}
