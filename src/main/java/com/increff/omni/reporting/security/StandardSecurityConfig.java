package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.AuthTokenFilter;
import com.increff.omni.reporting.config.ApplicationProperties;
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
@Order(2)
public class StandardSecurityConfig {

    @Autowired
    private AuthClient authClient;
    @Autowired
    private ApplicationProperties applicationProperties;

    public static final String APP_ADMIN = "app.admin";
    public static final String REPORT_ADMIN = "report.admin";
    public static final String REPORT_STANDARD = "report.standard";
    public static final String REPORT_CUSTOM = "report.custom";

    @Bean
    public SecurityFilterChain standardSecurityFilterChain(HttpSecurity http) throws Exception {

        http    //match only these URLs
                .securityMatcher("/standard/**")
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/standard/schedules/**").hasAnyAuthority(APP_ADMIN)//
                            .requestMatchers("/standard/pipelines/**").hasAnyAuthority(APP_ADMIN)//
                            .requestMatchers(HttpMethod.POST, "/standard/dashboards/{dashboardId}/view").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD, REPORT_CUSTOM)
                            .requestMatchers(HttpMethod.GET, "/standard/dashboards/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD, REPORT_CUSTOM)//
                            .requestMatchers("/standard/dashboards/**").hasAnyAuthority(APP_ADMIN)//
                            .requestMatchers("/standard/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD, REPORT_CUSTOM);
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthTokenFilter(authClient), BasicAuthenticationFilter.class)
                .addFilterAfter(new RateLimitingFilter(applicationProperties), AuthTokenFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public SecurityFilterChain getSecurityFilterChainForStaticResources(HttpSecurity http) throws Exception {
        http.securityMatcher("/v3/api-docs/**", "/swagger-ui.html", "swagger-ui/**")
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().permitAll();
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
