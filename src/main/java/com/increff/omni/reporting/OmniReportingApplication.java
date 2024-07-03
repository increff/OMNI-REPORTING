package com.increff.omni.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class,
        MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@ComponentScan({"com.increff.omni.reporting", "com.increff.account.client",
        "com.increff.commons.queryexecutor", "com.increff.commons.springboot.server"})
@EntityScan({"com.increff.omni.reporting", "com.increff.commons.springboot.audit"})
public class OmniReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(OmniReportingApplication.class, args);
    }


}