package com.increff.omni.reporting.model.form;


import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class OrgMappingsForm {
    @NotNull
    private Integer orgId;
    @NotNull
    private Integer schemaVersionId;
    @NotNull
    private Integer connectionId;
}
