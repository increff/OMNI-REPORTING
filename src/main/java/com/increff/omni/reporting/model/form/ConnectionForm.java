package com.increff.omni.reporting.model.form;

import com.increff.omni.reporting.model.constants.DBType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;

@Setter
@Getter
public class ConnectionForm {

    @NotEmpty
    private String name;
    @NotNull
    private String host;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private DBType dbType;

}
