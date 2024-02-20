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
@Order(1)
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private AdminFilter adminFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/admin/**")
                .and().authorizeRequests()//
                .antMatchers(HttpMethod.GET,"/admin/orgs").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole())
                .antMatchers(HttpMethod.POST, "/admin/request-report/orgs/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(),
                                Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole())
                .antMatchers(HttpMethod.GET,"/admin/reports/orgs/**").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole())
                .antMatchers(HttpMethod.GET,"/admin/orgs/*/reports/*/controls").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole())
                .antMatchers(HttpMethod.GET,"/admin/orgs/*/reports/live").hasAnyAuthority(Roles.APP_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole())
                .antMatchers("/admin/**").hasAnyAuthority(Roles.APP_ADMIN.getRole())//
                .and().cors().and().csrf().disable()
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(adminFilter, BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
                "/swagger-ui.html", "/webjars/**", "/ui/**", "/session/**");
    }
}
