package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.SchemaVersionForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class SchemaVersionData extends SchemaVersionForm {

    private Integer id;
    private ZonedDateTime updatedAt;

}
