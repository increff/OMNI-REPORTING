package com.increff.omni.reporting;

import com.increff.commons.springboot.common.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class,
        MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@ComponentScan({"com.increff.omni.reporting", "com.increff.account.client",
        "com.increff.commons.queryexecutor", "com.increff.commons.springboot.server"})
@EntityScan({"com.increff.omni.reporting", "com.increff.commons.springboot.audit"})
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