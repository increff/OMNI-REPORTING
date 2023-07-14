package com.increff.account.client;

import com.increff.account.model.*;
import com.nextscm.commons.spring.client.AbstractAppClient;
import com.nextscm.commons.spring.client.AppClientException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
public class AuthClient extends AbstractAppClient {

	public AuthClient(String baseUrl, String authAppToken) {
		super(baseUrl);
		HttpHeaders baseHeaders = new HttpHeaders();
		baseHeaders.add("authAppToken", authAppToken);
		setBaseHeaders(baseHeaders);
	}

	public AuthClient(String baseUrl, String authAppToken, RestTemplate restTemplate) {
		super(baseUrl, restTemplate);
		HttpHeaders baseHeaders = new HttpHeaders();
		baseHeaders.add("authAppToken", authAppToken);
		setBaseHeaders(baseHeaders);
	}

	public QueryUserData getUser(String domainName, String username) throws AppClientException {
		String fullUrl = "/query/api/domain-user/" + domainName + "/" + username + "/";
		return makeRequest(HttpMethod.GET, fullUrl, null, null, null, QueryUserData.class);
	}

	public List<QueryUserData> getUsers(String domainName) throws AppClientException {
		String fullUrl = "/query/api/domain-user/" + domainName + "/";
		return Arrays.asList(makeRequest(HttpMethod.GET, fullUrl, null, null, null, QueryUserData[].class));
	}

	// Provide domainName, username and credentials
	@Cacheable(value = "users", key = "#form.username + #form.credential + #form.domainName", unless = "#result.status != true ")
	public QueryUserData authenticate(QueryUserForm form) throws AppClientException {
		return makeRequest(HttpMethod.POST, "/query/api/authenticate", null, null, form, QueryUserData.class);
	}

	// Provide domainName, username and duration of token
	public QueryTokenData createToken(QueryUserForm form) throws AppClientException {
		return makeRequest(HttpMethod.POST, "/query/api/token", null, null, form, QueryTokenData.class);
	}

	@Cacheable(value = "users", key = "#token", unless = "#result.status != true ")
	public QueryUserData veriftyToken(String token) throws AppClientException {
		return makeRequest(HttpMethod.GET, "/query/api/token/" + token, null, null, null, QueryUserData.class);
	}

	public void deleteToken(String token) throws AppClientException {
		makeRequest(HttpMethod.DELETE, "/query/api/token/" + token, null, null, null, QueryTokenData.class);
	}

	public QueryTokenData convertTempToken(String tempToken) throws AppClientException {
		return makeRequest(HttpMethod.GET, "/query/api/temptoken/" + tempToken, null, null, null, QueryTokenData.class);
	}

	// Provide domainName and username
	public QueryTokenData createJumpToken(QueryUserForm form) throws AppClientException {
		return makeRequest(HttpMethod.POST, "/query/api/jumptoken", null, null, form, QueryTokenData.class);
	}

	public List<AppResourceData> getAllAppResource(String appName) throws AppClientException {
		String fullUrl = "/query/api/resource/" + appName;
		return Arrays.asList(makeRequest(HttpMethod.GET, fullUrl, null, null, null, AppResourceData[].class));
	}

	public void createDomainResource(DomainResourceForm form) throws AppClientException {
		makeRequest(HttpMethod.POST, "/query/api/domain/resource", null, null, form, Void.class);
	}
}
