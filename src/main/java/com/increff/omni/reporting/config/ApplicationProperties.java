package com.increff.omni.reporting.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class ApplicationProperties {

    @Value("${user.report.request.corePoolSize:100}")
    private Integer userReportRequestCorePool;

    @Value("${user.report.request.maxPoolSize:200}")
    private Integer userReportRequestMaxPool;

    // Reason for default queue capacity 0 is we have implemented report job in such
    // a way that if we have free core pool then only we will assign a thread
    @Value("${user.report.request.queueCapacity:0}")
    private Integer userReportRequestQueueCapacity;

    @Value("${schedule.report.request.corePoolSize:100}")
    private Integer scheduleReportRequestCorePool;

    @Value("${schedule.report.request.maxPoolSize:200}")
    private Integer scheduleReportRequestMaxPool;

    // Reason for default queue capacity 0 is we have implemented report job in such
    // a way that if we have free core pool then only we will assign a thread
    @Value("${schedule.report.request.queueCapacity:0}")
    private Integer scheduleReportRequestQueueCapacity;

    @Value("${stuck.report.time.minutes:10}")
    private Integer stuckReportTime;

    @Value("${max.execution.time.seconds:300}")
    private Integer maxExecutionTime;

    @Value("${live.report.max.execution.time.seconds:30}")
    private Integer liveReportMaxExecutionTime;

    @Value("${max.file.size.mb:200}")
    private Integer maxFileSize;

    @Value("${auth.baseUrl}")
    private String authBaseUrl;

    @Value("${auth.appToken}")
    private String authAppToken;

    @Value("${query.executor.baseUrl}")
    private String queryExecutorBaseUrl;

    @Value("${query.executor.authDomainName}")
    private String queryExecutorAuthDomain;

    @Value("${query.executor.authUsername}")
    private String queryExecutorAuthUsername;

    @Value("${query.executor.authPassword}")
    private String queryExecutorAuthPassword;



    @Value("${max.schedule.limit:15}")
    private Integer maxScheduleLimit;

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

    @Value("${from.email}")
    private String fromEmail;

    @Value("${mailjet.username}")
    private String username;

    @Value("${mailjet.password}")
    private String password;

    @Value("${mailjet.smtp.host}")
    private String smtpHost;

    @Value("${mailjet.smtp.port}")
    private String smtpPort;

}
