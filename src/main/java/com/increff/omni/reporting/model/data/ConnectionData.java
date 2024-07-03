package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.DBType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectionData {

    private Integer id;
    private String name;
    private String host;
    private String username;
    private DBType dbType;
}
