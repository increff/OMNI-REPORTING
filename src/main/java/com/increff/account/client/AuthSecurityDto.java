package com.increff.account.client;

import com.increff.account.model.*;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Service
public class AuthSecurityDto {

	@Autowired
	private AuthClient client;

	@Autowired
	private CacheManager cacheManager;

	protected Cookie loginPost(boolean authStatus, String authMessage, String authTempToken) throws ApiException {
		if (!authStatus) {
			throw new ApiException(ApiStatus.AUTH_ERROR, authMessage);
		}
		if (authTempToken == null) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Temporary token is not present");
		}

		QueryTokenData qtd;
		try {
			qtd = client.convertTempToken(authTempToken);
		} catch (AppClientException e) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error in creating Permanent Token: " + e.getMessage());
		}
		if (!qtd.isSuccessful()) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "User not authenticated, message: " + qtd.getMessage());
		}

		Cookie c = new Cookie(Params.AUTH_TOKEN, qtd.getToken());
		c.setHttpOnly(Boolean.TRUE);
		c.setSecure(Boolean.TRUE);
		return c;

	}

	protected List<AppResourceData> getAllAppResourceData(String appName) throws ApiException {
		try {
			return client.getAllAppResource(appName);
		}catch (AppClientException e){
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error in getting All App Resource: " + e.getMessage());
		}
	}

	//DO we need this, as this should be a part of internal app client call.
	protected void createDomainResource(DomainResourceForm form) throws ApiException {
		try {
			 client.createDomainResource(form);
		}catch (AppClientException e){
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error in creating Domain Resource : " + e.getMessage());
		}
	}

	protected void logout() throws ApiException {
		HttpSession session = getSession();
		if (session != null) {
			session.invalidate();
		}
		String authToken = getToken();
		if (authToken == null) {
			return;
		}
		try {
			client.deleteToken(authToken);
		} catch (AppClientException e) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error in deleting Permanent Token: " + e.getMessage());
		}
	}

	protected QueryUserData getCurrentUser() throws ApiException {
		String authToken = getToken();
		if (authToken == null) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Permanent token is not present");
		}
		QueryUserData qud;
		try {
			qud = client.veriftyToken(authToken);
		} catch (AppClientException e) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error in verifying Permanent Token: " + e.getMessage());
		}
		if (!qud.isStatus()) {
			throw new ApiException(ApiStatus.AUTH_ERROR, qud.getMessage());
		}
		return qud;
	}

	protected String getJumpToken() throws ApiException {
		QueryUserData qud = getCurrentUser();
		String userName = qud.getUsername();
		String domain = qud.getDomainName();
		QueryUserForm form = new QueryUserForm();
		form.setDomainName(domain);
		form.setUsername(userName);

		QueryTokenData qtd = null;
		try {
			qtd = client.createJumpToken(form);
		} catch (AppClientException e) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "Error while creating Jump Token" + e.getMessage());
		}
		if (!qtd.isSuccessful()) {
			throw new ApiException(ApiStatus.AUTH_ERROR, "User not authenticated " + qtd.getMessage());
		}
		return qtd.getToken();
	}

	// HTTP REQUEST, RESPONSE AND SESSION RELATED HELPER METHODS
	protected void setAttribute(String name, String value) {
		getSession().setAttribute(name, value);
	}

	protected String getAttribute(String name) {
		return (String) getSession().getAttribute(name);
	}

	protected HttpSession getSession() {
		HttpServletRequest req = getHttpRequest();
		return req.getSession();
	}

	public void evictBasicUser(String username, String password) {
		cacheManager.getCache(Params.USERS).evict(username + password);
	}

	public void evictAuthUser() {
		cacheManager.getCache(Params.USERS).evict(getToken());
	}

	public void evictSingleCacheValue(String cacheName, String cacheKey) {
		cacheManager.getCache(cacheName).evict(cacheKey);
	}

	private HttpServletRequest getHttpRequest() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest req = sra.getRequest();
		return req;
	}

	// AUTHETNICATION RELATED HELPER METHODS
	private String getToken() {
		HttpServletRequest req = getHttpRequest();
		String authToken = CookieUtil.getCookie(req, Params.AUTH_TOKEN);
		return authToken;
	}

}
