package com.increff.omni.reporting.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


@Log4j
public class SecurityFilterUtil {

    private static final String REPORT_ID_STRING = "reportId";

    public static String getFromJsonPayload(HttpServletRequest request, ObjectMapper objectMapper) throws ApiException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("application/json")) {
            try {
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
