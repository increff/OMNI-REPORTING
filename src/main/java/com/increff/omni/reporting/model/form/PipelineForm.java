package com.increff.omni.reporting.model.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.increff.omni.reporting.model.constants.FileProviderType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PipelineForm {
    @NotNull
    private String name;
    @NotNull
    private FileProviderType type;
    @NotNull
    private JsonNode configs;
}
