package com.increff.omni.reporting.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(-1) 
public class EmbedSecurityConfig {

    @Autowired
    private EmbedTokenAuthenticationFilter embedTokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain embedSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/embed/**")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/embed/**").authenticated();
                })
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable);

        http.addFilterBefore(embedTokenAuthenticationFilter, BasicAuthenticationFilter.class)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}