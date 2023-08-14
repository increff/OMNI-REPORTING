package com.increff.omni.reporting.security;

import com.increff.account.client.AuthTokenFilter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
@Order(2)
@Log4j
public class StandardSecurityConfig {

    @Autowired
    private AuthTokenFilter authTokenFilter;

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";
    private static final String REPORT_STANDARD = "report.standard";

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http// Match only these URLs
//                .requestMatchers()//
//                .antMatchers("/standard/**").and().authorizeRequests()//
//                .antMatchers("/standard/schedules/**").hasAnyAuthority(APP_ADMIN)
//                .antMatchers("/standard/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN, REPORT_STANDARD)//
//                .and().cors().and().csrf().disable()
//                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.cors();
//    }

    @Bean
    @Qualifier("standardSecurityFilterChain")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/standard/schedules/**").hasAnyAuthority(APP_ADMIN)
                .requestMatchers("/standard/**").hasAnyAuthority(APP_ADMIN,REPORT_ADMIN,REPORT_STANDARD))
                .cors().and().csrf().disable()
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
        return http.build();

    }

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui.html", "/webjars/**", "/ui/**"
//                , "/session/**");
//    }

    @Bean
    @Qualifier("standardWebSecurityCustomizer")
    public WebSecurityCustomizer standardWebSecurityCustomizer(){
        return web -> web.ignoring().requestMatchers("/v3/api-docs/**", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui/**", "/webjars/**", "/ui/**"
                , "/session/**");
    }

//    @Override
//    public void init(SecurityBuilder builder) throws Exception {
//
//    }
//
//    @Override
//    public void configure(SecurityBuilder builder) throws Exception {
//    }

}
