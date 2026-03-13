package com.increff.omni.reporting.security;

import com.increff.account.client.UserPrincipal;
import com.increff.commons.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Log4j2
@Component
public class EmbedTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public EmbedTokenAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.debug("EmbedTokenAuthenticationFilter processing request: {}", requestUri);

        try {

            String token = extractBearerToken(request);

            if (token != null) {
                log.debug("Found Bearer token in request to: {}", requestUri);

                if (jwtUtil.validateToken(token)) {
                
                    String userId = jwtUtil.getUsernameFromToken(token);
                    String email = jwtUtil.getEmailFromToken(token);
                    String orgName = jwtUtil.getOrgFromToken(token);
                    UserPrincipal userPrincipal = new UserPrincipal();
                    userPrincipal.setUsername(userId);
                    userPrincipal.setEmail(email);
                    userPrincipal.setFullName(orgName); 
                    List<String> roles = Collections.singletonList("embed.user");
                    userPrincipal.setRoles(roles);
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("embed.user")
                    );


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userPrincipal, token, authorities);


                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("Authentication set in SecurityContext for userId: {}, orgName: {}", userId, orgName);
                } else {
                    log.warn("JWT token validation failed for request to: {}", requestUri);
                }
            } else {
                log.debug("No Bearer token found in request to: {}", requestUri);
            }
        } catch (Exception e) {
            log.error("Error validating JWT token for request to: {}", requestUri, e);
            // Don't set authentication - let it fail through to unauthorized
        }

        filterChain.doFilter(request, response);
    }

  
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); 
        }

        return null;
    }
}