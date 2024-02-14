package com.increff.omni.reporting.model.form.FileProviderFolder;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GcpPipelineConfigForm extends AbstractPipelineConfigForm {
    private JsonNode credentialsJson;
}
