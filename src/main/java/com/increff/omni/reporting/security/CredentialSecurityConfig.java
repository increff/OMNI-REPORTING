package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.AuthTokenFilter;
import com.increff.account.client.CredentialFilter;
import com.increff.omni.reporting.model.constants.Roles;

import java.util.Arrays;
import java.util.List;

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
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@EnableWebSecurity
@Order(0) // Higher priority than StandardSecurityConfig (Order 2) to match this endpoint first
public class CredentialSecurityConfig {

    private static final List<String> SECURED_ROLES = Arrays.asList(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.ICC_REPORT_STANDARD.getRole(), Roles.ICC_REPORT_CUSTOM.getRole(), Roles.APP_INTEGRATION.getRole());

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

        AuthTokenFilter authTokenFilter = new AuthTokenFilter(authClient);

        CredentialFilter credentialFilter = new CredentialFilter(authClient);

        http    //match these specific URLs that need CredentialFilter
                .securityMatcher(new OrRequestMatcher(
                        new RegexRequestMatcher("/standard/app-access/dashboards/\\d+/view", null),
                        new RegexRequestMatcher("/standard/dashboards/\\d+", "GET")
                ))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new RegexRequestMatcher("/standard/app-access/dashboards/\\d+/view", null))
                            .hasAnyAuthority(SECURED_ROLES.toArray(String[]::new))
                        .requestMatchers(new RegexRequestMatcher("/standard/dashboards/\\d+", "GET"))
                            .hasAnyAuthority(SECURED_ROLES.toArray(String[]::new))
                )
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                // Adding them in this order ensures: authTokenFilter executes first, then credentialFilter, then RoleOverrideFilter
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(credentialFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(roleOverrideFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, CredentialFilter.class)
                .addFilterAfter(reportAppAccessFilter, RateLimitingFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}

