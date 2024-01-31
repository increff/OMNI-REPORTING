package com.increff.omni.reporting;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.increff.commons.springboot.common.JsonUtil;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@ComponentScan({"com.increff.omni.reporting", "com.increff.account.client", "com.increff.commons.queryexecutor", "com.increff.commons.springboot.audit"})
public class OmniReportingApplication {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(OmniReportingApplication.class, args);
    }

    @PostConstruct
    public void printDataSourceProps() throws SQLException {
        System.out.println("DataSource Name :" + JsonUtil.serialize(dataSource.getClass().getName()));
        System.out.println("DataSource Metadata :" + dataSource.getConnection().getMetaData());
    }

}