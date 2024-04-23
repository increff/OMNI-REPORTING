package com.increff.omni.reporting.security;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.Roles;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@Log4j2
public class RoleOverrideFilter extends GenericFilterBean {

    @Bean
    public FilterRegistrationBean<RoleOverrideFilter> tenantFilterRegistration(RoleOverrideFilter filter) {
        FilterRegistrationBean<RoleOverrideFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        UserPrincipal userPrincipal = SecurityUtil.getPrincipal();

        if(Objects.nonNull(userPrincipal)) {
            List<String> roles = userPrincipal.getRoles();
            log.debug("Roles before override: " + roles);
            if (roles.stream().anyMatch(role -> role.equalsIgnoreCase(Roles.REPORT_STANDARD.getRole()))) {
                if (roles.stream().noneMatch(role -> role.equalsIgnoreCase(Roles.OMNI_REPORT_STANDARD.getRole()))) {
                    roles.add(Roles.OMNI_REPORT_STANDARD.getRole()); // Add role to userPrincipal if not present
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(Roles.OMNI_REPORT_STANDARD.getRole());
                    SecurityUtil.addAuthority(authority);
                }
            }

            if (roles.stream().anyMatch(role -> role.equalsIgnoreCase(Roles.REPORT_CUSTOM.getRole()))) {
                if (roles.stream().noneMatch(role -> role.equalsIgnoreCase(Roles.OMNI_REPORT_CUSTOM.getRole()))) {
                    roles.add(Roles.OMNI_REPORT_CUSTOM.getRole()); // Add role to userPrincipal if not present
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(Roles.OMNI_REPORT_CUSTOM.getRole());
                    SecurityUtil.addAuthority(authority);
                }
            }

            userPrincipal.setRoles(roles);
            log.debug("Roles after override: " + userPrincipal.getRoles());
        }
        chain.doFilter(request, response);
    }
}
