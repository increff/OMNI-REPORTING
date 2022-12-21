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

    // Reason for default queue capacity 0 is we have implemented report job in such
    // a way that if we have free core pool then only we will assign a thread
    @Value("${async.queueCapacity:0}")
    private Integer queueCapacity;

    @Value("${stuck.report.time.minutes:10}")
    private Integer stuckReportTime;

    @Value("${max.execution.time.minutes:5}")
    private Integer maxExecutionTime;

    @Value("${gcp.baseUrl:dummy}")
    private String gcpBaseUrl;

    @Value("${gcp.bucketName}")
    private String gcpBucketName;

    @Value("${gcp.filePath}")
    private String gcpFilePath;

    @Value("${root.directory:root}")
    private String rootDirectory;

    @Value("${increff.orgId}")
    private Integer increffOrgId;

    @Value("${query.outDir:omni-reporting-files}")
    private String outDir;

    @Value("${scheduler.run}")
    private Boolean isRunScheduler;

    @Value("${reporting.version}")
    private String version;

}
