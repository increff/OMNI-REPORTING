package com.increff.omni.reporting.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.increff.account.client.AuthClient;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.util.FileUploadUtil;
import com.nextscm.commons.fileclient.AbstractFileProvider;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.fileclient.GcpFileProvider;
import com.nextscm.commons.spring.audit.api.AuditApi;
import com.nextscm.commons.spring.audit.dao.AuditDao;
import com.nextscm.commons.spring.audit.dao.DaoProvider;
import com.nextscm.commons.spring.server.WebMvcConfig;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableScheduling
@EnableAsync
@ComponentScan({"com.increff.omni.reporting", "com.increff.account.client"})
@PropertySource(value = "file:omni-reporting.properties")
@PropertySources({ //
        @PropertySource(value = "classpath:config.properties"), //
        @PropertySource(value = "file:omni-reporting.properties")
})
@Import({WebMvcConfig.class})
public class SpringConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public FileClient getFileClient() throws FileClientException {
        AbstractFileProvider gcpFileProvider = getGcpFileProvider();
        return new FileClient(gcpFileProvider);
    }

    @Bean
    public GcpFileProvider getGcpFileProvider() throws FileClientException {
        return new GcpFileProvider(applicationProperties.getGcpBaseUrl(),
                applicationProperties.getGcpBucketName(), applicationProperties.getGcpFilePath());
    }

    @Bean
    public FileUploadUtil getFileUploadUtil() throws IOException {
        return new FileUploadUtil(
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
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(getMapper());
        return jsonConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customJackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods("*");
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
        connManager.setDefaultMaxPerRoute(100);
        connManager.setMaxTotal(100);
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse httpResponse, HttpContext httpContext) {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (httpResponse.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase
                            ("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 30 * 1000L;
            }
        };

        HttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).setKeepAliveStrategy(myStrategy).build();

        /* HttpComponentsClientHttpRequestFactory is being used as this gives more flexibility around timeouts.
         * Also one must be aware that when ever HttpComponentsClientHttpRequestFactory is used, default connection
         * manager is PoolingHttpClientConnectionManager. And in case the default one is used, the connections configurations
         * are too small as mentioned above. So when using HttpComponentsClientHttpRequestFactory, one must properly
         * configure PoolingHttpClientConnectionManager else problems are expected at scale*/
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(15 * 1000);
        factory.setReadTimeout(25 * 1000);

        return factory;
    }

}
