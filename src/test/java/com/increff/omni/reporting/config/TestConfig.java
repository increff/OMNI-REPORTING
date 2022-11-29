package com.increff.omni.reporting.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.nextscm.commons.fileclient.AbstractFileProvider;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.fileclient.GcpFileProvider;
import com.nextscm.commons.spring.audit.api.AuditApi;
import com.nextscm.commons.spring.audit.dao.AuditDao;
import com.nextscm.commons.spring.audit.dao.DaoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@EnableWebMvc
@ComponentScan(value = {"com.increff.omni.reporting", "com.increff.account.client"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SpringConfig.class}))
@PropertySource("classpath:com/increff/omni/reporting/test.properties")
public class TestConfig {

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

}
