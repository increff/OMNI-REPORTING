package com.increff.omni.reporting.security;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.commons.springboot.common.JsonUtil;
import com.increff.commons.springboot.common.FieldErrorData;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class AdminFilter extends GenericFilterBean {

    private ApplicationProperties properties;

    public AdminFilter(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        UserPrincipal userPrincipal = SecurityUtil.getPrincipal();
        if (userPrincipal == null) {
            unAuthenticateCall(HttpStatus.UNAUTHORIZED, httpResponse, "Access Denied");
            return;
        }
        if (!properties.getIncreffOrgId().equals(userPrincipal.getDomainId())) {
            unAuthenticateCall(HttpStatus.FORBIDDEN, httpResponse, "This request is only accessible by Increff Admins");
            return;
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
            log.error("Error while writing un-authenticate response : ", e);
        }
    }
}
