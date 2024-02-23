package com.increff.omni.reporting.model.form.FileProviderFolder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwsPipelineConfigForm extends AbstractPipelineConfigForm {
    private String region;
    private String accessKey;
    private String secretKey;
}
