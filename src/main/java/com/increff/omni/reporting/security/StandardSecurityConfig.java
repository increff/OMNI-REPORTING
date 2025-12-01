package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.AuthTokenFilter;
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
@Order(2)
public class StandardSecurityConfig {

    @Autowired
    private AuthClient authClient;
    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private ReportAppAccessFilter reportAppAccessFilter;

    @Autowired
    private RoleOverrideFilter roleOverrideFilter;

    @Bean
    public SecurityFilterChain standardSecurityFilterChain(HttpSecurity http) throws Exception {

        http    //match only these URLs (but exclude the specific /view endpoint and GET dashboard endpoint which are handled by CredentialSecurityConfig)
                .securityMatcher(request -> {
                    String path = request.getServletPath();
                    String method = request.getMethod();
                    // Match /standard/**
                    if (!path.startsWith("/standard/")) {
                        return false;
                    }
                    // Exclude /standard/app-access/dashboards/{dashboardId}/view
                    if (path.matches("/standard/app-access/dashboards/\\d+/view")) {
                        return false;
                    }
                    // Exclude GET /standard/dashboards/{dashboardId}
                    if (HttpMethod.GET.matches(method) && path.matches("/standard/dashboards/\\d+")) {
                        return false;
                    }
                    return true;
                })
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/standard/schedules/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                            .requestMatchers("/standard/pipelines/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                            .requestMatchers( "/standard/dashboards/send-dashboard").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.ICC_REPORT_STANDARD.getRole(), Roles.ICC_REPORT_CUSTOM.getRole()  )//                       
                            .requestMatchers( "/standard/reports/benchmark").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.ICC_REPORT_STANDARD.getRole(), Roles.ICC_REPORT_CUSTOM.getRole()  )//                       
                            .requestMatchers(HttpMethod.GET, "/standard/dashboards/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.ICC_REPORT_STANDARD.getRole(), Roles.ICC_REPORT_CUSTOM.getRole()  )//
                            .requestMatchers("/standard/dashboards/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                            .requestMatchers("/standard/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.ICC_REPORT_STANDARD.getRole(), Roles.ICC_REPORT_CUSTOM.getRole()  );
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthTokenFilter(authClient), BasicAuthenticationFilter.class)
                .addFilterBefore(roleOverrideFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, AuthTokenFilter.class)
                .addFilterAfter(reportAppAccessFilter, RateLimitingFilter.class)
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
