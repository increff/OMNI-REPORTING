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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/standard/**").and().authorizeRequests()//
                .antMatchers("/standard/schedules/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers("/standard/pipelines/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers(HttpMethod.POST, "/standard/dashboards/{dashboardId}/view").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.UNIFY_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.UNIFY_REPORT_CUSTOM.getRole())//
                .antMatchers(HttpMethod.GET, "/standard/dashboards/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.UNIFY_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.UNIFY_REPORT_CUSTOM.getRole())//
                .antMatchers("/standard/dashboards/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .antMatchers("/standard/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.OMNI_REPORT_STANDARD.getRole(), Roles.UNIFY_REPORT_STANDARD.getRole(), Roles.OMNI_REPORT_CUSTOM.getRole(), Roles.UNIFY_REPORT_CUSTOM.getRole())//
                .and().cors().and().csrf().disable() // todo : add filter
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, AuthTokenFilter.class)
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
