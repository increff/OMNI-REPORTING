package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.commons.springboot.common.HealthBulkData;
import com.increff.commons.springboot.server.AbstractHealthDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.increff.commons.springboot.server.HealthUtil.dbHealthCheck;
import static com.increff.commons.springboot.server.HealthUtil.serviceHealthCheck;


@Log4j2
@Service
public class HealthDto extends AbstractHealthDto {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public HealthBulkData healthCheck(Boolean dependencies) {
        HealthBulkData healthBulkData = new HealthBulkData();
        if(Objects.nonNull(dependencies) && dependencies) {
            healthBulkData.getHealthDataMap().put("crypto", serviceHealthCheck(applicationProperties.getCryptoHealthUrl()));
            healthBulkData.getHealthDataMap().put("account", serviceHealthCheck(applicationProperties.getAccountHealthUrl()));
            healthBulkData.getHealthDataMap().put("queryExecutor", serviceHealthCheck(applicationProperties.getQueryExecutorHealthUrl()));
//            healthBulkData.getHealthDataMap().put("db", dbHealthCheck(applicationProperties.getJdbcUrl(),
//                    applicationProperties.getJdbcUsername(), applicationProperties.getJdbcPassword()));
            // TODO - add connection pojo health checks later if required
        }
        return healthBulkData;
    }
}
