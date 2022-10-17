package com.increff.omni.reporting.security;

import com.increff.account.client.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

    private static final String APP_ADMIN = "app.admin";
    public static final String APP_STANDARD = "app.standard";

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/standard/**").and().authorizeRequests()//
                .antMatchers("/standard/**").hasAnyAuthority(APP_ADMIN, APP_STANDARD)//
                // Ignore CSRF and CORS
                .and().csrf().disable().cors().disable().addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class).sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**", "/ui/**", "/session/**");
    }
}
