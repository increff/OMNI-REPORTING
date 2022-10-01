package com.increff.omni.reporting.config;

import com.nextscm.commons.fileclient.AbstractFileProvider;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.fileclient.GcpFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableScheduling
@EnableWebMvc
@EnableAsync
@ComponentScan({"com.increff.omni.reporting"})
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

}
