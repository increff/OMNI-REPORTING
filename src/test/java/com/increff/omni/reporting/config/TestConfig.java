package com.increff.omni.reporting.config;

import com.nextscm.commons.fileclient.AbstractFileProvider;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.fileclient.GcpFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// Todo complete tests
@Configuration
@EnableScheduling
@EnableWebMvc
@EnableAsync
@ComponentScan(value = {"com.increff.omni.reporting", "com.increff.account.client"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SpringConfig.class}))
@PropertySource("classpath:com/increff/omni/reporting/test.properties")
public class TestConfig {

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

}
