package com.increff.omni.reporting.config;

import com.nextscm.commons.fileclient.AbstractFileProvider;
import com.nextscm.commons.fileclient.FileClient;
import com.nextscm.commons.fileclient.FileClientException;
import com.nextscm.commons.fileclient.GcpFileProvider;
import com.nextscm.commons.spring.server.WebMvcConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

/**
 * TOP-MOST level Spring configuration file, that starts the Spring
 * configuration
 */

/**
 * Spring configuration for loading application properties.
 */
@Configuration
@ComponentScan({ "com.increff.omni.reporting"})
@PropertySources({ //
		@PropertySource("classpath:com/increff/omni/reporting/config.properties")/*, //
		@PropertySource(value = "file:./omni-reporting.properties")*///
})
@Import({ WebMvcConfig.class })
public class SpringConfig {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean
	public FileClient getFileClient() throws FileClientException {
		AbstractFileProvider gcpFileProvider = new GcpFileProvider(applicationProperties.getGcpBaseUrl(),
				applicationProperties.getGcpBucketName(), applicationProperties.getGcpFilePath());
		return new FileClient(gcpFileProvider);
	}

}
