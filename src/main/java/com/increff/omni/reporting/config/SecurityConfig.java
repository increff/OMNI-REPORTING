//package com.increff.omni.reporting.config;
//
//import com.increff.account.client.AuthClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//
//@Configuration
//@EnableWebSecurity(debug = true)
//public class SecurityConfig {
//
//    @Autowired
//    private AuthClient authClient;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.securityMatcher("/admin/**", "/super/**", "/client/**", "/file/**", "/order/**", "/box", "/box/**",
//                        "/log", "/shipment", "/shipment/**", "/report/**")
//                .authorizeHttpRequests(auth -> {
//                    auth
//                            .requestMatchers("/admin/entity/**").hasAnyAuthority(AUTHORITY_ADMIN)//
//                            .requestMatchers("/admin/**").hasAnyAuthority(AUTHORITY_ISC_ADMIN, AUTHORITY_ISC_STANDARD)//
//                            .requestMatchers("/super/**").hasAnyAuthority(AUTHORITY_ADMIN)
//                            .requestMatchers("/client/**").hasAnyAuthority(AUTHORITY_ADMIN_STANDARD)
//                            .requestMatchers("/file/**").hasAnyAuthority(AUTHORITY_ADMIN_STANDARD)
//                            .requestMatchers("/order/**").hasAnyAuthority(AUTHORITY_ADMIN_STANDARD)
//                            .requestMatchers("/box", "/box/**").hasAnyAuthority(AUTHORITY_ADMIN_STANDARD)
//                            .requestMatchers("/shipment", "/shipment/**").hasAnyAuthority(AUTHORITY_ADMIN_STANDARD)
//                            .requestMatchers("/log").hasAnyAuthority(AUTHORITY_ISC_ADMIN, AUTHORITY_ISC_STANDARD)
//                            .requestMatchers("/report/**").hasAnyAuthority(AUTHORITY_ISC_ADMIN);
//                })
//                .cors(AbstractHttpConfigurer::disable)
//                .csrf(AbstractHttpConfigurer::disable)
//                .addFilterBefore(new AuthTokenFilter(authClient), BasicAuthenticationFilter.class)
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        return http.build();
//    }
//
//
//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring().requestMatchers("/v3/api-docs/**", "/configuration/ui", "/swagger-resources", "/configuration/security",
//                "/swagger-ui", "/webjars/**", "/ui/**", "swagger-ui/**");
//    }
//
//}