package com.increff.omni.reporting.security;

import com.increff.account.client.AuthTokenFilter;
import com.increff.omni.reporting.model.constants.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(2)
public class StandardSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthTokenFilter authTokenFilter;
    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private ReportAppAccessFilter reportAppAccessFilter;

    @Autowired
    private RoleOverrideFilter roleOverrideFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/standard/**").and().authorizeRequests()//
                .antMatchers("/standard/schedules/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers("/standard/pipelines/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers(HttpMethod.POST, "/standard/dashboards/{dashboardId}/view").access("@roleUtil.hasAdminOrStandardOrCustom(authentication)")// todo : test hasAdminOrStandardOrCustom after springboot merge
                .antMatchers(HttpMethod.GET, "/standard/dashboards/**").access("@roleUtil.hasAdminOrStandardOrCustom(authentication)")//
                .antMatchers("/standard/dashboards/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers("/standard/**").access("@roleUtil.hasAdminOrStandardOrCustom(authentication)")//
                .and().cors().and().csrf().disable()
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(roleOverrideFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, RoleOverrideFilter.class)
                .addFilterAfter(reportAppAccessFilter, RateLimitingFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**", "/ui/**"
                , "/session/**");
    }
}
