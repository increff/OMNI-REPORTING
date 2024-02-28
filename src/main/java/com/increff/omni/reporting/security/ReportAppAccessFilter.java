package com.increff.omni.reporting.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.controller.AppAccessController;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.util.UserPrincipalUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Log4j
@Component
public class ReportAppAccessFilter extends GenericFilterBean {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ReportApi reportApi;

    private static final String REPORT_ID_STRING = "reportId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        try {
            Object controller = getControllerByURL((HttpServletRequest) request);
            if(Objects.nonNull(controller) && !controller.getClass().equals(AppAccessController.class))
                chain.doFilter(request, response);


            String reportIdStr = extractReportId((HttpServletRequest) request);
            if(Objects.isNull(reportIdStr))
                throw new ApiException(ApiStatus.BAD_DATA, "Report Id not found in request");
            Integer reportId = Integer.parseInt(reportIdStr);

            ReportPojo report = reportApi.getByIdAndAppNameIn(reportId, UserPrincipalUtil.getAccessibleApps());
            if(Objects.isNull(report))
                throw new ApiException(ApiStatus.BAD_DATA, "User does not have access to report");

            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
            // ques : should i throw runtime exception here? error when increff throwing api exception
            //'doFilter(ServletRequest, ServletResponse, FilterChain)' in 'com.increff.omni.reporting.security.ReportAppAccessFilter' clashes with 'doFilter(ServletRequest, ServletResponse, FilterChain)' in 'javax.servlet.Filter'; overridden method does not throw 'com.nextscm.commons.spring.common.ApiException'

        }

    }

//      todo : remove comment
//    private static Map<String, List<String>> getHeadersMap(HttpServletRequest httpRequest) {
//        return Collections.list(httpRequest.getHeaderNames())
//                .stream()
//                .collect(Collectors.toMap(
//                        Function.identity(),
//                        h -> Collections.list(httpRequest.getHeaders(h))
//                ));
//    }

    public Object getControllerByURL(HttpServletRequest request) throws Exception {
        HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(request);
        if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
            return handlerMethod.getBean();
        }
        return null;
    }

    private String extractReportId(HttpServletRequest request) throws ApiException{
        String reportId = getFromQueryParameter(request); // Check if the report id is in the query parameter
        if (reportId != null) return reportId;

        reportId = getFromPathVariable(request); // Check if the report id is in the path variable
        if (reportId != null) return reportId;

        reportId = getFromJsonPayload(request); // Check if the request contains a JSON body
        if (reportId != null) return reportId;

        return null;
    }

    private static String getFromJsonPayload(HttpServletRequest request) throws ApiException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("application/json")) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(request.getReader());

                JsonNode reportIdNode = jsonNode.get("reportId");
                if (reportIdNode != null) {
                    return reportIdNode.asText();
                }
            } catch (IOException e) {
                log.error("ReportAppAccessFilter Error reading JSON payload" + e.getMessage() + Arrays.asList(e.getStackTrace()));
                throw new ApiException(ApiStatus.BAD_DATA, "ReportAppAccessFilter Error reading JSON payload " + e.getMessage());
            }
        }
        return null;
    }

    private static String getFromPathVariable(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            String[] pathSegments = pathInfo.split("/");
            for (String segment : pathSegments) {
                if (segment.startsWith(REPORT_ID_STRING + "=")) {
                    return segment.substring(REPORT_ID_STRING.length()+1); // added 1 for equals sign
                }
            }
        }
        return null;
    }

    private static String getFromQueryParameter(HttpServletRequest request) {
        return request.getParameter(REPORT_ID_STRING);
    }

}