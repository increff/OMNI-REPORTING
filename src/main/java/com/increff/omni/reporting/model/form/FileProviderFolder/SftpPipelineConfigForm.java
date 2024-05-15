package com.increff.omni.reporting.model.form.FileProviderFolder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SftpPipelineConfigForm extends AbstractPipelineConfigForm {
    private String host;
    private String username;
    private String password;
}
