package com.increff.omni.reporting.model.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.increff.omni.reporting.model.form.FileProviderFolder.AbstractPipelineConfigForm;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineConfigData extends AbstractPipelineConfigForm {
}
