package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class OrganizationForm {

    @NotNull
    private Integer id;

    @NotNull
    private String name;

}
