package com.increff.omni.reporting.model.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.increff.omni.reporting.model.constants.FileProviderType;
import com.increff.omni.reporting.model.form.PipelineForm;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class PipelineData {
    private String name;
    private FileProviderType type;
    private Integer id;
    private PipelineConfigData configs;
}
