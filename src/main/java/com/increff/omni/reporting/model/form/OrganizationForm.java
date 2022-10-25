package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class OrganizationForm {

    @NotNull
    private Integer id;

    @NotEmpty
    private String name;

}
