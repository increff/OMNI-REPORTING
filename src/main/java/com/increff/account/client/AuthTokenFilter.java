package com.increff.account.client;

import com.increff.account.model.QueryUserData;
import com.nextscm.commons.spring.client.AppClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//Filters request to see if valid user
//Always use this after CredentialFilter
@Component
public class AuthTokenFilter extends GenericFilterBean {

	@Autowired
	private AuthClient authClient;

	@Override
	public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		// Check if user is authenticated
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse resp = (HttpServletResponse) res;
		if(!checkHeaders(hreq)) {
			unAutheticateCall(resp);
			return;
		}
		QueryUserData qd = getQueryUserData(hreq);
		boolean authenticated = qd != null && qd.isStatus();
		if (qd != null && !qd.isStatus()) {
			unAutheticateCall(resp);
			return;
		}
		if (!authenticated) {
			// Clear any existing authentication token
			SecurityUtil.setAuthentication(null);
		}
		else {
			// If authentication token does not exist then set it up
			Authentication auth = SecurityUtil.getAuthentication();

			// TODO: Ideally we should not be checking for auth=null. We should be setting
			// new authentication for every API call (as internally Authentication object is
			// stored in a ThreadLocal variable).
			// But for some reason, if UI is making multiple calls then not checking for
			// auth=null breaks the authentication flow.
			if (auth == null) {
				auth = AuthenticationUtil.getAuthenticationToken(qd);
				SecurityUtil.setAuthentication(auth);
			}
		}

		chain.doFilter(req, res);

	}

	public QueryUserData getQueryUserData(HttpServletRequest hreq) throws IOException {
		String authToken = CookieUtil.getCookie(hreq, Params.AUTH_TOKEN);
		if (authToken == null) {
			return null;
		}
		try {
			return authClient.veriftyToken(authToken);
		} catch (AppClientException e) {
			throw new IOException("Error in authclient invocation", e);
		}
	}

	private Boolean checkHeaders(HttpServletRequest hreq) {
		String domainName = hreq.getHeader("authDomainName");
		String username = hreq.getHeader("authUsername");
		String credential = hreq.getHeader("authPassword");
		String authToken = CookieUtil.getCookie(hreq, Params.AUTH_TOKEN);
		Boolean credentialCheck = StringUtils.isEmpty(domainName) || StringUtils.isEmpty(username) || StringUtils.isEmpty(credential);
		if (StringUtils.isEmpty(authToken) && credentialCheck) {
			return false;
		}
		return true;
	}

	private void unAutheticateCall(HttpServletResponse response) {
		SecurityUtil.setAuthentication(null);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

}
