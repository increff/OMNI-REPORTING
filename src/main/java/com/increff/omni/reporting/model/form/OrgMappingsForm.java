package com.increff.omni.reporting.model.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.increff.omni.reporting.model.constants.PipelineType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
