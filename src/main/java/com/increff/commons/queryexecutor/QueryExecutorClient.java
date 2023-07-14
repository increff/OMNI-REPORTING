package com.increff.commons.queryexecutor;

import com.increff.commons.queryexecutor.data.QueryRequestData;
import com.increff.commons.queryexecutor.form.GetRequestForm;
import com.increff.commons.queryexecutor.form.QueryExecutorForm;
import com.nextscm.commons.spring.client.AbstractAppClient;
import com.nextscm.commons.spring.client.AppClientException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class QueryExecutorClient extends AbstractAppClient {

    private HttpHeaders baseHeaders;


    private static final String SUBMIT_REQUEST = "/requests/submit";

    private static final String REQUESTS = "/requests";

    public QueryExecutorClient(String baseUrl) {
        super(baseUrl);
    }

    public QueryExecutorClient(String baseUrl, String authDomainName, String authUsername,
                               String authPassword, RestTemplate restTemplate) {
        super(baseUrl, restTemplate);
        this.baseHeaders = getHeaders(authDomainName, authUsername, authPassword);
    }

    public void postRequest(QueryExecutorForm form) throws AppClientException {
        makeRequest(HttpMethod.POST, SUBMIT_REQUEST, baseHeaders, null, form, Void.class);
    }

    public List<QueryRequestData> getRequests(GetRequestForm form) throws AppClientException {
        return Arrays.asList(makeRequest(HttpMethod.POST, REQUESTS, baseHeaders, null, form,
                QueryRequestData[].class));
    }

    private HttpHeaders getHeaders(String authDomainName, String authUsername, String authPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("authDomainName", authDomainName);
        headers.set("authUsername", authUsername);
        headers.set("authPassword", authPassword);
        return headers;
    }
}
