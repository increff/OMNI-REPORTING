package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ConnectionForm {

    @NotNull
    private String name;
    @NotNull
    private String host;
    @NotNull
    private String username;
    @NotNull
    private String password;

}
