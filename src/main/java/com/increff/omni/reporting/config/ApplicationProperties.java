package com.increff.omni.reporting.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ApplicationProperties {

    @Value("${report.request.corePoolSize:100}")
    private Integer reportRequestCorePool;

    @Value("${report.request.maxPoolSize:200}")
    private Integer reportRequestMaxPool;

    // Reason for default queue capacity 0 is we have implemented report job in such
    // a way that if we have free core pool then only we will assign a thread
    @Value("${report.request.queueCapacity:0}")
    private Integer reportRequestQueueCapacity;

    @Value("${report.schedule.corePoolSize:100}")
    private Integer reportScheduleCorePool;

    @Value("${report.schedule.maxPoolSize:200}")
    private Integer reportScheduleMaxPool;

    // Reason for default queue capacity 0 is we have implemented report job in such
    // a way that if we have free core pool then only we will assign a thread
    @Value("${report.schedule.queueCapacity:0}")
    private Integer reportScheduleQueueCapacity;

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

    @Value("${reporting.version}")
    private String version;

}
