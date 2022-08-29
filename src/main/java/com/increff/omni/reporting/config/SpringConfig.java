package com.increff.omni.reporting.config;

import com.nextscm.commons.spring.server.WebMvcConfig;
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

}
