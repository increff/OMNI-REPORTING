package com.increff.omni.reporting.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.bson.json.JsonObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.ws.RequestWrapper;
import java.io.*;
import java.security.Principal;
import java.util.*;


@Log4j
public class SecurityFilterUtil {

    private static final String REPORT_ID_STRING = "reportId";

    public static String getFromJsonPayload(HttpServletRequest request, ObjectMapper objectMapper) throws ApiException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.startsWith("application/json")) {
            try {
                // Retrieve request body as a string
                // String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

                // Parse the JSON string using ObjectMapper
                JsonNode jsonNode = objectMapper.readTree(getJsonRequestBody(request));

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

    public static String getJsonRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new StringReader(request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()))));
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        return json;
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

    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
}
