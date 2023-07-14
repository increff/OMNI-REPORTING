package com.increff.account.client;

import com.increff.account.model.QueryUserData;
import com.increff.account.model.QueryUserForm;
import com.nextscm.commons.spring.client.AppClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//Filters request to see if valid user
//TODO will not throw 401 when credentials and authToken both are not present
@Component
public class CredentialFilter extends GenericFilterBean {

	@Autowired
	private AuthClient authClient;

	@Override
	public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hresp = (HttpServletResponse) res;
		if(!checkHeaders(hreq)) {
			unAutheticateCall(hresp);
			return;
		}
		QueryUserData qd = getQueryUserData(hreq);
		boolean authenticated = qd != null && qd.isStatus();
		if (authenticated) {
			Authentication auth = AuthenticationUtil.getAuthenticationToken(qd);
			SecurityUtil.setAuthentication(auth);
		}
		else if (qd != null) {
			unAutheticateCall(hresp);
			return;
		}
		try {
			chain.doFilter(req, hresp);
		} finally {
			if (authenticated) {
				SecurityUtil.setAuthentication(null);
			}

		}

	}

	public QueryUserData getQueryUserData(HttpServletRequest hreq) throws IOException {
		// Try with username and password
		String domainName = hreq.getHeader("authDomainName");
		String username = hreq.getHeader("authUsername");
		String credential = hreq.getHeader("authPassword");

		if (username == null || credential == null) {
			return null;
		}
		// Authenticate domain & token
		QueryUserForm f = new QueryUserForm();
		f.setDomainName(domainName);
		f.setUsername(username);
		f.setCredential(credential);
		try {
			return authClient.authenticate(f);
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
