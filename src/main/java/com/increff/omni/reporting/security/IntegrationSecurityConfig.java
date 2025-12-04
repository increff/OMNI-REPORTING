package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.CredentialFilter;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(3)
public class IntegrationSecurityConfig {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean
    public SecurityFilterChain integrationSecurityFilterChain(HttpSecurity http) throws Exception {

        http    //match only these URLs
                .securityMatcher("/integration/**")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/integration/**").hasAnyAuthority(Roles.APP_INTEGRATION.getRole());
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new CredentialFilter(authClient), BasicAuthenticationFilter.class)
                .addFilterBefore(new AdminFilter(applicationProperties), BasicAuthenticationFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
