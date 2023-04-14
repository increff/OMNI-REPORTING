package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Setter
@Getter
public class SchemaVersionForm {

    @NotEmpty
    private String name;
}
