package com.increff.omni.reporting.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Log4j2
public class SecurityFilterUtil {

    private static final String REPORT_ID_STRING = "reportId";

    public static String getFromJsonPayload(ContentCachingRequestWrapper wrappedRequest, ObjectMapper objectMapper) throws ApiException {
        String contentType = wrappedRequest.getContentType();
        if (contentType != null && contentType.startsWith("application/json")) {
            try {
                JsonNode jsonNode = objectMapper.readTree(getJsonRequestBody(wrappedRequest));
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

    public static String getJsonRequestBody(ContentCachingRequestWrapper wrappedRequest) throws IOException {
        String requestBody = IOUtils.toString(wrappedRequest.getInputStream(), StandardCharsets.UTF_8);
        log.debug("Request body: " + requestBody);
        return requestBody;
    }


    public static String getFromPathVariable(HttpServletRequest request) {
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

    public static String getFromQueryParameter(HttpServletRequest request) {
        return request.getParameter(REPORT_ID_STRING);
    }

}