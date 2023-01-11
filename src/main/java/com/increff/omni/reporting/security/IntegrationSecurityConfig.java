package com.increff.omni.reporting.security;

import com.increff.account.client.CredentialFilter;
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
@Order(3)
public class IntegrationSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CredentialFilter credentialFilter;

    @Autowired
    private AdminFilter adminFilter;

    private static final String APP_INTEGRATION = "app.integration";


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http// Match only these URLs
                .requestMatchers()//
                .antMatchers("/integration/**")
                .and().authorizeRequests()//
                .antMatchers("/integration/**").hasAnyAuthority(APP_INTEGRATION)//
                .and().cors().and().csrf().disable()
                .addFilterBefore(credentialFilter, BasicAuthenticationFilter.class)
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
