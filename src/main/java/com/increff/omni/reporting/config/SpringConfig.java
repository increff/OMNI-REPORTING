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
import com.nextscm.commons.spring.server.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TOP-MOST level Spring configuration file, that starts the Spring
 * configuration
 */

/**
 * Spring configuration for loading application properties.
 */
@Configuration
@EnableScheduling
@EnableAsync
@ComponentScan({"com.increff.omni.reporting", "com.increff.account.client"})
@PropertySource(value = "file:omni-reporting.properties")
@Import({WebMvcConfig.class})
public class SpringConfig {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public FileClient getFileClient() throws FileClientException {
        AbstractFileProvider gcpFileProvider = new GcpFileProvider(applicationProperties.getGcpBaseUrl(),
                applicationProperties.getGcpBucketName(), applicationProperties.getGcpFilePath());
        return new FileClient(gcpFileProvider);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
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

}
