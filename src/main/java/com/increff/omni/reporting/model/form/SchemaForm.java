package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class SchemaForm {

    @NotNull
    private String name;
}
