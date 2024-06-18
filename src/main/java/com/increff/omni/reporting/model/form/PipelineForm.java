package com.increff.omni.reporting.model.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.increff.omni.reporting.model.constants.PipelineType;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class PipelineForm {
    @NotNull
    private String name;
    @NotNull
    private PipelineType type;
    @NotNull
    private JsonNode configs;
}
