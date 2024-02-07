package com.increff.omni.reporting.model.form.FileProviderFolder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwsFileProviderForm extends AbstractFileProviderForm {
    private String awsRegion;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsBucketName;
    private String awsBucketUrl;
}
