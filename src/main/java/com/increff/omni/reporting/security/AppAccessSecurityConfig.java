package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.AuthTokenFilter;
import com.increff.account.client.CredentialFilter;
import com.increff.omni.reporting.model.constants.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(0) // Higher priority than StandardSecurityConfig (Order 2) to match this endpoint first
public class AppAccessSecurityConfig {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private RoleOverrideFilter roleOverrideFilter;

    @Autowired
    private ReportAppAccessFilter reportAppAccessFilter;

    @Bean
    public SecurityFilterChain appAccessSecurityFilterChain(HttpSecurity http) throws Exception {

        http    //match only this specific URL
                .securityMatcher("/standard/app-access/dashboards/{dashboardId}/view")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, "/standard/app-access/dashboards/{dashboardId}/view")
                            // Allow roles from AuthTokenFilter (standard user roles)
                            .hasAnyAuthority(
                                    Roles.APP_ADMIN.getRole(),
                                    Roles.REPORT_ADMIN.getRole(),
                                    Roles.OMNI_REPORT_STANDARD.getRole(),
                                    Roles.OMNI_REPORT_CUSTOM.getRole(),
                                    Roles.ICC_REPORT_STANDARD.getRole(),
                                    Roles.ICC_REPORT_CUSTOM.getRole(),
                                    // Allow role from CredentialFilter (integration role)
                                    Roles.APP_INTEGRATION.getRole()
                            );
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthTokenFilter(authClient), BasicAuthenticationFilter.class)
                .addFilterBefore(new CredentialFilter(authClient), BasicAuthenticationFilter.class)
                // Add RoleOverrideFilter to handle role overrides for AuthTokenFilter users
                .addFilterBefore(roleOverrideFilter, BasicAuthenticationFilter.class)
                // Add RateLimitingFilter after authentication (matches StandardSecurityConfig pattern)
                .addFilterAfter(rateLimitingFilter, AuthTokenFilter.class)
                .addFilterAfter(reportAppAccessFilter, RateLimitingFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}

