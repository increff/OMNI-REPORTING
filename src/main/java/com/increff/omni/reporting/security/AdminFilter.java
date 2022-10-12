package com.increff.omni.reporting.security;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.nextscm.commons.spring.common.FieldErrorData;
import com.nextscm.commons.spring.common.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AdminFilter extends GenericFilterBean {

    @Autowired
    private ApplicationProperties properties;

    private static final String APP_ADMIN = "app.admin";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        UserPrincipal userPrincipal = SecurityUtil.getPrincipal();
        if (userPrincipal == null) {
            unAuthenticateCall(HttpStatus.UNAUTHORIZED, httpResponse, "Access Denied");
            return;
        }
        if (!properties.getIncreffOrgId().equals(userPrincipal.getDomainId())) {
            if (userPrincipal.getRoles().contains(APP_ADMIN))
                unAuthenticateCall(HttpStatus.FORBIDDEN, httpResponse, "This request is only accessible by Increff Admins");
        }
        chain.doFilter(request, response);
    }

    private static void unAuthenticateCall(HttpStatus status, HttpServletResponse response, String responseBody) {
        response.setStatus(status.value());
        response.setContentType("application/json");

        FieldErrorData errorData = new FieldErrorData();
        errorData.setField(status.name());
        errorData.setCode(String.valueOf(status.value()));
        errorData.setMessage(responseBody);
        try {
            response.getWriter().write(JsonUtil.serialize(errorData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
