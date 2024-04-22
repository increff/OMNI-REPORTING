package com.increff.omni.reporting.security;

import com.increff.account.client.AuthClient;
import com.increff.account.client.AuthTokenFilter;
import com.increff.omni.reporting.config.ApplicationProperties;
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
@Order(1)
public class AdminSecurityConfig {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private ApplicationProperties applicationProperties;

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {

        http    //match only these URLs
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(HttpMethod.GET,"/admin/orgs").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole())
                            .requestMatchers(HttpMethod.POST, "/admin/request-report/orgs/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole())
                            .requestMatchers(HttpMethod.GET,"/admin/reports/orgs/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole())
                            .requestMatchers(HttpMethod.GET,"/admin/orgs/*/reports/*/controls").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole())
                            .requestMatchers(HttpMethod.GET,"/admin/orgs/*/reports/live").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole())
                            .requestMatchers("/admin/**").hasAnyAuthority(Roles.APP_ADMIN.getRole());
                })
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new AuthTokenFilter(authClient), BasicAuthenticationFilter.class)
                .addFilterBefore(new AdminFilter(applicationProperties), BasicAuthenticationFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
