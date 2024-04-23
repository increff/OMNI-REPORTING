package com.increff.omni.reporting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.controller.AppAccessController;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static com.increff.omni.reporting.util.SecurityFilterUtil.*;

@Log4j2
@Component
public class ReportAppAccessFilter extends GenericFilterBean {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public FilterRegistrationBean<ReportAppAccessFilter> tenantFilterRegistration(ReportAppAccessFilter filter) {
        FilterRegistrationBean<ReportAppAccessFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try {
            Object controller = getControllerByURL((HttpServletRequest) request);
            if( (Objects.isNull(controller)) || (!controller.getClass().equals(AppAccessController.class)) ) {// Continue if controller not found or controller is not AppAccessController
                chain.doFilter(request, response);
                return;
            }

            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
            // This line is necessary to cache InputStream (If request goes forward without this line, getContentAsByteArray() will return empty array)
            // This gets further called in getJsonPayload() method. If this line is removed and getJsonPayload is later refactored, it will cause issues
            wrappedRequest.getInputStream();

            String reportIdStr = extractReportId(wrappedRequest);
            if(Objects.isNull(reportIdStr))
                throw new ApiException(ApiStatus.BAD_DATA, "Report Id not found in request");
            Integer reportId = Integer.parseInt(reportIdStr);
            log.debug("ReportAppAccessFilter.ReportId : " + reportId + " userAccessibleApps : " + UserPrincipalUtil.getAccessibleApps() + " username : " + UserPrincipalUtil.getPrincipal().getUsername());

            ReportPojo report = reportApi.getByIdAndAppNameIn(reportId, UserPrincipalUtil.getAccessibleApps());
            if(Objects.isNull(report))
                throw new ApiException(ApiStatus.BAD_DATA, "User does not have access to report");

            chain.doFilter(wrappedRequest, response);
            log.debug("ReportAppAccessFilter.doFilter end");
        } catch (Exception e) {
            setResponse((HttpServletResponse) response, e);
        }
    }

    private void setResponse(HttpServletResponse response, Exception e) throws IOException {
        int responseCode = 403;
        HttpServletResponse httpResponse = response;
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write("{\"status\":\"" + responseCode + "\",\"message\":\"" + e.getMessage() + "\"}");
        httpResponse.setStatus(responseCode);
    }

    public Object getControllerByURL(HttpServletRequest request) throws Exception {
        HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);
        if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
            return handlerMethod.getBean();
        }
        return null;
    }

    private String extractReportId(ContentCachingRequestWrapper request) throws ApiException {
        String reportId = null;

        reportId = getFromJsonPayload(request, objectMapper); // Check if the request contains a JSON body
        if (reportId != null) return reportId;

        reportId = getFromQueryParameter(request); // Check if the report id is in the query parameter
        if (reportId != null) return reportId;

        reportId = getFromPathVariable(request); // Check if the report id is in the path variable
        if (reportId != null) return reportId;

        return null;
    }



}