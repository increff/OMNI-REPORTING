package com.increff.omni.reporting.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.increff.account.client.AuthClient;
import com.increff.commons.queryexecutor.QueryExecutorClient;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.util.FileDownloadUtil;
import com.increff.service.encryption.EncryptionClient;
import com.nextscm.commons.spring.audit.api.AuditApi;
import com.nextscm.commons.spring.audit.dao.AuditDao;
//import com.nextscm.commons.spring.audit.dao.DaoProvider;
import com.increff.omni.reporting.commons.DaoProvider;
//import org.apache.http.HeaderElement;
//import org.apache.http.HeaderElementIterator;
//import org.apache.http.client.HttpClient;
//import org.apache.http.conn.ConnectionKeepAliveStrategy;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//import org.apache.http.message.BasicHeaderElementIterator;
//import org.apache.http.protocol.HTTP;


import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HeaderElements;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeaderElementIterator;
import org.apache.hc.core5.http.message.HttpResponseWrapper;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

@Configuration
@EnableWebMvc
@ComponentScan(value = {"com.increff.omni.reporting", "com.increff.account.client", "com.increff.commons.queryexecutor"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SpringConfig.class}))
@PropertySource("classpath:com/increff/omni/reporting/test.properties")
public class TestConfig {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public FileDownloadUtil getFileUploadUtil() throws IOException {
        return new FileDownloadUtil(
                applicationProperties.getGcpBucketName(), applicationProperties.getGcpFilePath());
    }

    @Bean(name = "objectMapper")
    public ObjectMapper getMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaTimeModule javaTimeModule=new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class,
                new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        return Jackson2ObjectMapperBuilder.json().featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ISODate
                .modules(javaTimeModule).build();
    }

    @Bean
    public EncryptionClient getEncryptionClient(){
        return new EncryptionClient(applicationProperties.getCryptoBaseUrl());
    }

    @Bean
    public AuditDao auditDao() {
        return new AuditDao();
    }

    @Bean
    @Autowired
    public AuditApi auditApi(DaoProvider daoProvider) {
        AuditApi auditApi = new AuditApi();
        auditApi.setProvider(daoProvider);
        return auditApi;
    }

    @Bean
    public AuthClient getAuthClient() {
        return new AuthClient(applicationProperties.getAuthBaseUrl(), applicationProperties.getAuthAppToken(),
                new RestTemplate(getRequestFactory()));
    }

    @Bean
    public QueryExecutorClient getQueryExecutorClient() {
        return new QueryExecutorClient(applicationProperties.getQueryExecutorBaseUrl(),
                applicationProperties.getQueryExecutorAuthDomain(),
                applicationProperties.getQueryExecutorAuthUsername(),
                applicationProperties.getQueryExecutorAuthPassword(),
                new RestTemplate(getRequestFactory()));
    }

//    private ClientHttpRequestFactory getRequestFactory() {
//
//        /* HttpClient by default uses BasicHttpClientConnectionManager which uses single connection object and hence
//         * is not preferred to be used in case the application is heavy on rest call.
//         * Also we must provide correct value for max total and max per route as the default value for these are
//         * just 20 and 2 respectively. This means at a time only 2 parallel request can be processed for a particular host.
//         * In application like proxy, we do have a lot of parallel requests going for same host and hence this value
//         * should be higher. Also the container tomcat has 200 default number of threads configured. As in proxy all the
//         * requests are supposed to make external calls, we have use these decisively.
//         * Suggested value is 100 for a heavy proxy and 50 for light weight proxy*/
//        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
//        connManager.setDefaultMaxPerRoute(100);
//        connManager.setMaxTotal(100);
//        ConnectionKeepAliveStrategy myStrategy = (httpResponse, httpContext) -> {
//            HeaderElementIterator it = new BasicHeaderElementIterator
//                    (httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
//            while (it.hasNext()) {
//                HeaderElement he = it.nextElement();
//                String param = he.getName();
//                String value = he.getValue();
//                if (value != null && param.equalsIgnoreCase
//                        ("timeout")) {
//                    return Long.parseLong(value) * 1000;
//                }
//            }
//            return 30 * 1000L;
//        };
//
//        HttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).setKeepAliveStrategy(myStrategy).build();
//
//        /* HttpComponentsClientHttpRequestFactory is being used as this gives more flexibility around timeouts.
//         * Also one must be aware that when ever HttpComponentsClientHttpRequestFactory is used, default connection
//         * manager is PoolingHttpClientConnectionManager. And in case the default one is used, the connections configurations
//         * are too small as mentioned above. So when using HttpComponentsClientHttpRequestFactory, one must properly
//         * configure PoolingHttpClientConnectionManager else problems are expected at scale*/
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//        factory.setConnectTimeout(15 * 1000);
//        factory.setReadTimeout(25 * 1000);
//
//        return factory;
//    }

    private ClientHttpRequestFactory getRequestFactory() {

        /* HttpClient by default uses BasicHttpClientConnectionManager which uses single connection object and hence
         * is not preferred to be used in case the application is heavy on rest call.
         * Also we must provide correct value for max total and max per route as the default value for these are
         * just 20 and 2 respectively. This means at a time only 2 parallel request can be processed for a particular host.
         * In application like proxy, we do have a lot of parallel requests going for same host and hence this value
         * should be higher. Also the container tomcat has 200 default number of threads configured. As in proxy all the
         * requests are supposed to make external calls, we have use these decisively.
         * Suggested value is 100 for a heavy proxy and 50 for light weight proxy*/
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(applicationProperties.getMaxConnectionsPerRoute());
        connManager.setMaxTotal(applicationProperties.getMaxConnections());
        connManager.setDefaultSocketConfig(
                SocketConfig.custom()
                        .setSoTimeout(Timeout.ofSeconds(25)) // Set your desired socket read timeout
                        .build()
        );

        final ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public TimeValue getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                final Iterator<HeaderElement> it = MessageSupport.iterate(httpResponse, HeaderElements.KEEP_ALIVE);
                final HeaderElement he = it.next();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return TimeValue.ofSeconds(Long.parseLong(value));
                    } catch (final NumberFormatException ignore) {
                    }
                }
                return TimeValue.ofSeconds(30);
            }
        };

        HttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).setKeepAliveStrategy(myStrategy).build();

        /* HttpComponentsClientHttpRequestFactory is being used as this gives more flexibility around timeouts.
         * Also one must be aware that when ever HttpComponentsClientHttpRequestFactory is used, default connection
         * manager is PoolingHttpClientConnectionManager. And in case the default one is used, the connections configurations
         * are too small as mentioned above. So when using HttpComponentsClientHttpRequestFactory, one must properly
         * configure PoolingHttpClientConnectionManager else problems are expected at scale*/
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(15 * 1000);
        factory.setConnectTimeout(15 * 1000);

        return factory;
    }
}
