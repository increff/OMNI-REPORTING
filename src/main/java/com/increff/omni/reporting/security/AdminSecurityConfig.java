package com.increff.omni.reporting.security;

import com.increff.account.client.AuthTokenFilter;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@Order(1)
//@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
public class AdminSecurityConfig {
    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private AdminFilter adminFilter;

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//
//        http// Match only these URLs
//                .requestMatchers()//
//                .antMatchers("/admin/**")
//                .and().authorizeRequests()//
//                .antMatchers(HttpMethod.GET,"/admin/orgs").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)
//                .antMatchers(HttpMethod.POST, "/admin/request-report/orgs/**").hasAnyAuthority(APP_ADMIN,
//                        REPORT_ADMIN)
//                .antMatchers(HttpMethod.GET,"/admin/reports/orgs/**").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)
//                .antMatchers(HttpMethod.GET,"/admin/orgs/*/reports/*/controls").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)
//                .antMatchers(HttpMethod.GET,"/admin/orgs/*/reports/live").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)
//                .antMatchers("/admin/**").hasAnyAuthority(APP_ADMIN)//
//                .and().cors().and().csrf().disable()
//                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
//                .addFilterBefore(adminFilter, BasicAuthenticationFilter.class)
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        http.cors();
//    }

    @Bean
    @Qualifier("adminFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests.requestMatchers(HttpMethod.GET, "/admin/orgs").hasAnyAuthority(APP_ADMIN, REPORT_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/admin/request-report/orgs/**").hasAnyAuthority(APP_ADMIN,
                                REPORT_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/admin/request-report/orgs/**").hasAnyAuthority(APP_ADMIN,
                                REPORT_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/admin/reports/orgs/**").hasAnyAuthority(APP_ADMIN,
                                REPORT_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/admin/orgs/*/reports/*/controls").hasAnyAuthority(APP_ADMIN,
                                REPORT_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/admin/orgs/*/reports/live").hasAnyAuthority(APP_ADMIN,
                                REPORT_ADMIN)
                        .requestMatchers("/admin/**").hasAnyAuthority(APP_ADMIN))//
                .cors().and().csrf().disable()
                .addFilterBefore(authTokenFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(adminFilter, BasicAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors();
        return http.build();
    }

    //    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
//                "/swagger-ui.html", "/webjars/**", "/ui/**", "/session/**");
//    }
    @Bean
    @Qualifier("adminWebSecurityCustomizer")
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/v3/api-docs/**", "/configuration/ui", "/swagger-resources", "/configuration/security", "/swagger-ui/**", "/webjars/**", "/ui/**", "/session/**");
    }
}
