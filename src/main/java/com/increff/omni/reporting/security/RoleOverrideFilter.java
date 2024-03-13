package com.increff.omni.reporting.security;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.Roles;
import com.nextscm.commons.spring.common.FieldErrorData;
import com.nextscm.commons.spring.common.JsonUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@Log4j
public class RoleOverrideFilter extends GenericFilterBean {

    @Autowired
    private ApplicationProperties properties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        UserPrincipal userPrincipal = SecurityUtil.getPrincipal();
        if(Objects.nonNull(userPrincipal)) {
            List<String> roles = userPrincipal.getRoles();
            log.debug("Roles before override: " + roles);
            if (roles.stream().anyMatch(role -> role.equalsIgnoreCase(Roles.REPORT_STANDARD.getRole()))) {
                if (roles.stream().noneMatch(role -> role.equalsIgnoreCase(Roles.OMNI_REPORT_STANDARD.getRole())))
                    roles.add(Roles.OMNI_REPORT_STANDARD.getRole()); // Add role to userPrincipal if not present
            }

            if (roles.stream().anyMatch(role -> role.equalsIgnoreCase(Roles.REPORT_CUSTOM.getRole()))) {
                if (roles.stream().noneMatch(role -> role.equalsIgnoreCase(Roles.OMNI_REPORT_CUSTOM.getRole())))
                    roles.add(Roles.OMNI_REPORT_CUSTOM.getRole()); // Add role to userPrincipal if not present
            }
            log.debug("Roles after override: " + roles);
        }
        chain.doFilter(request, response);
    }
}
