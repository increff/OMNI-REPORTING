package com.increff.account.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class AuthSecurityConfig {

    @Autowired
    private AuthTokenFilter credentialFilter;

    public static final String domain_admin = "domain.admin";
    public static final String app_admin = "app.admin";

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//
//        http//
//                // Match only these URLs
//                .requestMatchers()//
//                .antMatchers("/session/domain-resource/**", "/session/app-resource/**")//
//                .and().authorizeRequests()//
//                .antMatchers("/session/domain-resource/**").hasAnyAuthority(domain_admin, app_admin)//
//                .antMatchers("/session/app-resource/**").hasAnyAuthority(domain_admin, app_admin)//
//                // Ignore CORS and CSRF
//                .and().cors().disable().csrf().disable()//
//                .addFilterBefore(credentialFilter, BasicAuthenticationFilter.class)//
//                // This ensures that all calls are session(JSESSIONID)
//                // independent
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//    }

    @Bean
    @Qualifier("authFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatchers()
                // Match only these URLs
                .requestMatchers("/session/domain-resource/**", "/session/app-resource/**").and().authorizeHttpRequests()
                .requestMatchers("/session/domain-resource/**").hasAnyAuthority(domain_admin,app_admin)
                .requestMatchers("/session/app-resource/**").hasAnyAuthority(domain_admin, app_admin)
                // Ignore CORS and CSRF
                .and().cors().and().csrf().disable()
                .addFilterBefore(credentialFilter, BasicAuthenticationFilter.class)
                // This ensures that all calls are session(JSESSIONID)
                // independent
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
        return http.build();
    }

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
//                "/swagger-ui.html", "/webjars/**");
//    }

    @Bean
    @Qualifier("authWebSecurityCustomizer")
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web.ignoring().requestMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources",
                "/configuration/security", "/swagger-ui.html", "/webjars/**");
    }
}
