package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.DBType;
import com.increff.omni.reporting.model.form.ConnectionForm;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ConnectionData {

    private Integer id;
    private String name;
    private String host;
    private String username;
    private DBType dbType;
}
