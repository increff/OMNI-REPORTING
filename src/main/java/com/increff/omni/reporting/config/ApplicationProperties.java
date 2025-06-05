package com.increff.omni.reporting.config;

import com.increff.omni.reporting.util.ConstantsUtil;
import com.increff.omni.reporting.util.MongoUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static com.increff.omni.reporting.util.ValidateUtil.UNIFY_QUERY_STRING;

@Getter
@Setter
@Component
public class ApplicationProperties {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

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

    @Value("${live.report.max.execution.time.seconds:300}")
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

    @Value("${max.connection.time:5}")
    private Integer maxConnectionTime;

    @Value("${resultset.fetch.size:1000}")
    private Integer resultSetFetchSize;

    @Value("${max.schedule.limit:15}")
    private Integer maxScheduleLimit;

    @Value("${gcp.bucketName}")
    private String gcpBucketName;

    @Value("#{${gcp.credentials}}")
    private Map<String, String> gcpCredentials;

    @Value("${root.directory:root}")
    private String rootDirectory;

    @Value("${increff.orgId}")
    private Integer increffOrgId;

    @Value("${query.outDir:omni-reporting-files}")
    private String outDir;

    @Value("${project.version}")
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

    @Value("${rest.max.connection.per.route:100}")
    private int maxConnectionsPerRoute;

    @Value("${rest.max.connection:200}")
    private int maxConnections;

    @Value("${crypto.base.url}")
    private String cryptoBaseUrl;

    @Value("${stuck.schedule.time.seconds:600}")
    private Integer stuckScheduleSeconds;

    @Value("${max.dashboards.per.org:100}")
    private Integer maxDashboardsPerOrg;

    @Value("${max.dashboard.charts:8}")
    private Integer maxDashboardCharts;

    @Value("${rate.limit.tokens.refill.amount:20}")
    private Integer tokens;
    @Value("${rate.limit.tokens.refill.rate.seconds:60}")
    private Integer tokensRefillRateSeconds;

    @Value("${crypto.health.url}")
    private String cryptoHealthUrl;

    @Value("${account.health.url}")
    private String accountHealthUrl;

    @Value("${query.executor.health.url}")
    private String queryExecutorHealthUrl;

    @Value("${mongo.read.timeout.seconds:300}")
    private Integer mongoReadTimeoutSec;

    @Value("${mongo.connect.timeout.seconds:60}")
    private Integer mongoConnectTimeoutSec;

    @Value("${max.retry.count:3}")
    private Integer maxRetryCount;

    @Value("${unify.query.string:mongoFilter(param}")
    private String unifyQueryString;

    @Value("${mongo.client.filter}")
    private String mongoClientFilter;

    @Value("${schedule.file.size.zip.after:5}")
    private Integer scheduleFileSizeZipAfter;

    @PostConstruct
    public void init() {
        UNIFY_QUERY_STRING = unifyQueryString;

        MongoUtil.MONGO_READ_TIMEOUT_SEC = mongoReadTimeoutSec;
        MongoUtil.MONGO_CONNECT_TIMEOUT_SEC = mongoConnectTimeoutSec;
        MongoUtil.MONGO_SERVER_SELECT_TIMEOUT_SEC = mongoConnectTimeoutSec;

        MongoUtil.MONGO_CLIENT_FILTER = mongoClientFilter;

        ConstantsUtil.MAX_RETRY_COUNT = maxRetryCount;

        ConstantsUtil.SCHEDULE_FILE_SIZE_ZIP_AFTER = scheduleFileSizeZipAfter;

    }

}
