package com.increff.omni.reporting.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ApplicationProperties {

    @Value("${async.corePoolSize:100}")
    private Integer corePoolSize;

    @Value("${async.maxPoolSize:200}")
    private Integer maxPoolSize;

    @Value("${async.queueCapacity:1000}")
    private Integer queueCapacity;

    @Value("${stuck.report.time:10}")
    private Integer stuckReportTime;

    @Value("${gcp.baseUrl}")
    private String gcpBaseUrl;

    @Value("${gcp.bucketName}")
    private String gcpBucketName;

    @Value("${gcp.filePath}")
    private String gcpFilePath;

    @Value("${root.directory}")
    private String rootDirectory;

    @Value("${increff.orgId}")
    private Integer increffOrgId;

}
