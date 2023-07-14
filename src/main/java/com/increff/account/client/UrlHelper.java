package com.increff.account.client;

import com.increff.account.model.UrlPath;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class UrlHelper {

	@Autowired
	private AuthConfig config;

	// ACCOUNT SERVER URLs

    protected String getAccountServerJumpUrl(UrlPath path, String jumpToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("jumpToken", jumpToken);
        if (path != null)
            params.add("nextUrl", path.getVal());
        return getFullUrlWithParams("/jump", params);
    }

	protected String getAccountServerRegisterUrl() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		return getFullUrlWithParams("/register/api/init", params);
	}

	protected String getAccountServerLoginUrl() {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		return getFullUrlWithParams("/auth/login", params);
	}

	// LOCAL APPLICATION URLs
	protected String getAuthNextUrl() throws ApiException {
		String appBaseUrl = getAppBaseUrl();
		return appBaseUrl + "/session/login-post";
	}

	protected String getRegisterNextUrl() throws ApiException {
		String appBaseUrl = getAppBaseUrl();
		return appBaseUrl + "/session/register-post";
	}

	// PRIVATE HELPER METHODS
	private String getFullUrlWithParams(String path, MultiValueMap<String, String> params) {
		String fullUrl1 = config.getAuthBaseUrl() + path;
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(fullUrl1).queryParams(params);
		return builder.buildAndExpand().toUriString();
	}

	private String getAppBaseUrl() {

		// Get HTTP request
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest req = sra.getRequest();

		// Did the request come via load-balance or proxy ? If yes, then via which
		// scheme?
		String forwardedScheme = req.getHeader("x-forwarded-proto");

		String scheme = req.getScheme();
		String port = ":" + req.getServerPort();
		String domain = req.getServerName();
		String contextPath = req.getContextPath();

		String appBaseUrl = null;
		if (forwardedScheme != null) {

			// We came through a loadbalancer. No need to give port. Standard is 443 or 80,
			// which gets determined by scheme anyway
			appBaseUrl = forwardedScheme + "://" + domain + contextPath;
		} else {
			// We were directly hit. So our scheme, port etc. are correct
			appBaseUrl = scheme + "://" + domain + port + contextPath;
		}
		return appBaseUrl;
	}

}
