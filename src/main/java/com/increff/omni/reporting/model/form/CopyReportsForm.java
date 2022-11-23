package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyReportsForm {

    private Integer oldSchemaVersionId;
    private Integer newSchemaVersionId;
}
