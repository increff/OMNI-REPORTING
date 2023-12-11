package com.increff.omni.reporting.security;

import com.increff.account.client.AuthTokenFilter;
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

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";
    private static final String REPORT_STANDARD = "report.standard";

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/standard/**").and().authorizeRequests()//
                .antMatchers("/standard/schedules/**").hasAnyAuthority(APP_ADMIN)
                .antMatchers("/standard/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD)//
                .antMatchers("/standard/dashboards/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)//
                .antMatchers(HttpMethod.GET, "/standard/dashboards/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD)//
                .antMatchers(HttpMethod.POST, "/standard/dashboards/{dashboardId}/view").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD)//
                .and().cors().and().csrf().disable()
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(rateLimitingFilter, AuthTokenFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**", "/ui/**"
                , "/session/**");
    }
}
