package com.nextscm.commons.spring.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextscm.commons.spring.common.ErrorData;
import com.nextscm.commons.spring.common.MapperUtil;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;

public class AbstractAppClient {

    private RestTemplate restTemplate;
    private ObjectMapper mapper;
    private String baseUrl;
    private HttpHeaders baseHeaders;


    public AbstractAppClient(String baseUrl) {
        this.mapper = MapperUtil.getMapper();
        this.baseHeaders = new HttpHeaders();
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplateFactory().getRestTemplate();
    }


    public AbstractAppClient(String baseUrl, RestTemplate restTemplate) {
        mapper = MapperUtil.getMapper();
        baseHeaders = new HttpHeaders();
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new AppResponseErrorHandler());

    }


    protected <T, R> R makeRequest(HttpMethod method, String path, HttpHeaders header,
                                   MultiValueMap<String, String> params, T request, Class<R> recieved) throws AppClientException {
        return makeRequestInternal(method, path, header, params, request, recieved);
    }

    protected <T, R> R makeRequest(HttpMethod method, String baseUrl, String path, HttpHeaders header,
                                   MultiValueMap<String, String> params, T request, Class<R> recieved) throws AppClientException {
        return makeRequestInternal(method, baseUrl, path, header, params, request, recieved);
    }

    protected <R> R makeMultipartRequest(HttpMethod method, String path, HttpHeaders header,
                                         MultiValueMap<String, String> params, MultiValueMap<String, Object> request, Class<R> received)
            throws AppClientException {
        return makeMultipartRequestInternal(method, path, header, params, request, received);
    }

    protected void setBaseHeaders(HttpHeaders baseHeaders) {
        this.baseHeaders = baseHeaders;
    }

    private <R> R makeMultipartRequestInternal(HttpMethod method, String path, HttpHeaders headers,
                                               MultiValueMap<String, String> params, MultiValueMap<String, Object> request, Class<R> received)
            throws AppClientException {
        String fullUrl = getFullUrlWithParams(path, params);
        HttpHeaders allHeaders = getHeaders();
        allHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        HttpEntity<MultiValueMap<String, Object>> payLoad = new HttpEntity<>(request, allHeaders);
        return makeRequestInternalFinal(method, fullUrl, payLoad, received);
    }

    private <T, R> R makeRequestInternal(HttpMethod method, String path, HttpHeaders headers,
                                         MultiValueMap<String, String> params, T request, Class<R> recieved) throws AppClientException {
        String fullUrl = getFullUrlWithParams(path, params);
        HttpHeaders allHeaders = getHeaders();
        allHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        HttpEntity<?> payload = new HttpEntity<>(request, allHeaders);
        return makeRequestInternalFinal(method, fullUrl, payload, recieved);
    }

    private <T, R> R makeRequestInternal(HttpMethod method,String baseUrl, String relativeUrl, HttpHeaders headers,
                                         MultiValueMap<String, String> params, T request, Class<R> recieved) throws AppClientException {
        String fullUrlWithParams = getUrlWithParams(params, getFullUrl(baseUrl, relativeUrl));
        HttpHeaders allHeaders = getHeaders();
        allHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            allHeaders.putAll(headers);
        }
        HttpEntity<?> payload = new HttpEntity<>(request, allHeaders);
        return makeRequestInternalFinal(method, fullUrlWithParams, payload, recieved);
    }

    private <T, R> R makeRequestInternalFinal(HttpMethod method, String fullUrl, HttpEntity<T> payload,
                                              Class<R> received) throws AppClientException {
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(fullUrl, method, payload, String.class);
        } catch (RestClientException t) {
            throw new AppClientException(AppClientStatus.IO_ERROR, "A remote IO error has occurred", t);
        }
        String responseBody = response.getBody();
        try {
            HttpStatusCode status = response.getStatusCode();
            if (status.is2xxSuccessful() && responseBody != null) {
                return received.equals(Void.class) ? null : mapper.readValue(responseBody, received);
            } else if (status.is2xxSuccessful()) {
                return null;
            } else {
                ErrorData error = mapper.readValue(responseBody, ErrorData.class);
                AppClientException ace = new AppClientException(AppClientStatus.RESPONSE_ERROR, error.getMessage(),
                        null);
                ace.setError(error);
                throw ace;
            }
        } catch (IOException e) {
            AppClientException ace = new AppClientException(AppClientStatus.PARSE_ERROR,
                    "Error parsing remote response", e);
            ace.setBody(responseBody);
            throw ace;
        }
    }

    public static void add(MultiValueMap<String, String> map, String key, Object[] values) {
        for (Object value : values) {
            map.add(key, value.toString());
        }
    }

    public static ByteArrayResource getResource(String fileName, InputStream is) {
        byte[] bytes = null;
        try {
            bytes = StreamUtils.copyToByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException("Error reading inputstream for:" + fileName);
        }
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    protected HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (baseHeaders != null) {
            headers.putAll(baseHeaders);
        }
        return headers;
    }

    protected String getFullUrlWithParams(String path, MultiValueMap<String, String> params) {
        String fullUrl = getFullUrl(path);
        return getUrlWithParams(params, fullUrl);
    }

    private String getUrlWithParams(MultiValueMap<String, String> params, String fullUrl) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(fullUrl).queryParams(params);
        fullUrl = builder.buildAndExpand().toUriString();
        return fullUrl;
    }

    protected String getFullUrl(String s) {
        return this.baseUrl + s;
    }

    private String getFullUrl(String baseUrl, String path) {
        return baseUrl + path;
    }
}
