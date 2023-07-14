package com.increff.account.client;

import com.increff.account.model.AppResourceData;
import com.increff.account.model.DomainResourceForm;
import com.increff.account.model.QueryUserData;
import com.increff.account.model.UrlPath;
import com.nextscm.commons.spring.common.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/*
 * This class is to provide application side controllers to manage login and logout
 * work flows. This class redirects the browser to "next URLs" after registration, login or logout.
 */

@RestController
@RequestMapping(value = "/session")
@CrossOrigin
@Slf4j
public class AuthSessionController {

	@Autowired
	private AuthSecurityDto dto;
	@Autowired
	private AuthConfig config;
	@Autowired
	private UrlHelper urlHelper;

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public RedirectView register(//
			RedirectAttributes rattribs, //
			@RequestParam(defaultValue = "na") String referrer, //
			@RequestParam(required = false) String utm_source, //
			@RequestParam(required = false) String utm_medium, //
			@RequestParam(required = false) String utm_campaign, //
			@RequestParam(required = false) String campaignname, //
			@RequestParam(required = false) String adgroupname, //
		    @RequestParam(required = false) String utm_term,
		    @RequestParam(required = false) String keyword,
		    @RequestParam(required = false) String matchtype,
		    @RequestParam(required = false) String network,
		    @RequestParam(required = false) String device,
		    @RequestParam(required = false) String creative,
			@RequestParam String appNextUrl //
	) throws Exception {
		// Invoke Auth server URL with authNextUrl set to this controller
		rattribs.addAttribute(Params.APP_NAME, config.getAuthAppName());
		rattribs.addAttribute(Params.REGISTER_NEXT_URL, urlHelper.getRegisterNextUrl());
		rattribs.addAttribute(Params.APP_REFERRER, referrer);

		populateRAttribsWithUTM(rattribs, utm_source, utm_medium, utm_campaign, campaignname, adgroupname, utm_term,
				keyword, matchtype, network, device, creative);

		// TODO Solve the session problem for horizontal scale
		dto.setAttribute(Params.APP_NEXT_URL, appNextUrl);
		return new RedirectView(urlHelper.getAccountServerRegisterUrl(), true);
	}

	@RequestMapping(value = "/register-post", method = RequestMethod.GET)
	public RedirectView registerPost(//
			@RequestParam boolean authStatus, //
			@RequestParam(required = false) String authTempToken, //
			@RequestParam(required = false) String authMessage//
	) throws ApiException {
		Cookie c = dto.loginPost(authStatus, authMessage, authTempToken);
		setCookieHeaderToResponse(c);
		String appNextUrl = dto.getAttribute(Params.APP_NEXT_URL);
		return new RedirectView(appNextUrl, true);
	}

	/*
	 * appPostLoginUrl should always be
	 * http://<domain>:port/<context>/session/login-post We need to take this value
	 * as we don't know if the app is deployed at
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public RedirectView login(//
			RedirectAttributes rattribs, //
			@RequestParam String appNextUrl, //
			@RequestParam(required = false) String domainName //
	) throws Exception {
		// Invoke Auth server URL with authNextUrl set to this controller
		rattribs.addAttribute(Params.APP_NAME, config.getAuthAppName());
		rattribs.addAttribute(Params.AUTH_NEXT_URL, urlHelper.getAuthNextUrl());
		if (domainName != null) {
			rattribs.addAttribute(Params.DOMAIN_NAME, domainName);
		}
		// TODO Solve the session problem for horizontal scale
		dto.setAttribute(Params.APP_NEXT_URL, appNextUrl);
		return new RedirectView(urlHelper.getAccountServerLoginUrl(), true);
	}

	@RequestMapping(value = "/login-post", method = RequestMethod.GET)
	public RedirectView loginPost(//
			@RequestParam boolean authStatus, //
			@RequestParam(required = false) String authTempToken, //
			@RequestParam(required = false) String authMessage//
	) throws ApiException {
		Cookie c = dto.loginPost(authStatus, authMessage, authTempToken);
		setCookieHeaderToResponse(c);
		String appNextUrl = dto.getAttribute(Params.APP_NEXT_URL);
		return new RedirectView(appNextUrl, true);
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public RedirectView logout(@RequestParam String appNextUrl) throws ApiException {
		dto.logout();
		dto.evictAuthUser();
		return new RedirectView(appNextUrl, true);
	}

    @RequestMapping(value = "/jump", method = RequestMethod.GET)
    public RedirectView jump(@RequestParam(required = false) UrlPath urlPath) throws ApiException {
        String jumpToken = dto.getJumpToken();
        String jumpUrl = urlHelper.getAccountServerJumpUrl(urlPath, jumpToken);
        return new RedirectView(jumpUrl, true);
    }

	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public QueryUserData checkActiveSession() throws ApiException {
		return dto.getCurrentUser();
	}

	@RequestMapping(value = "/evict", method = RequestMethod.PUT)
	public void evictBasicUser(@RequestParam(value = "user") String username,
							   @RequestParam(value = "password") String password) {
		dto.evictBasicUser(username, password);
	}

	@RequestMapping(value = "/domain-resource", method = RequestMethod.POST)
	public void createDomainResource(@RequestBody DomainResourceForm form) throws ApiException {
		dto.createDomainResource(form);
	}

	@RequestMapping(value = "/app-resource", method = RequestMethod.GET)
	public List<AppResourceData> getAllAppResource(@RequestParam(value = "appName") String appName) throws ApiException {
		return dto.getAllAppResourceData(appName);
	}

	private HttpServletResponse getHttpResponse() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletResponse res = sra.getResponse();
		return res;
	}
	private void setCookieHeaderToResponse(Cookie c) {
//		String string = c.getName().concat("=").concat(c.getValue()).concat("; Path=/; SameSite=None; secure");
//		getHttpResponse().addHeader("Set-Cookie", string);
		c.setPath(config.getAuthCookiePath());
		getHttpResponse().addCookie(c);
	}

	private void populateRAttribsWithUTM(RedirectAttributes rattribs, String utm_source, String utm_medium,
										 String utm_campaign, String campaignname, String adgroupname, String utm_term,
										 String keyword, String matchtype, String network, String device, String creative) {

		if (utm_source != null)
			rattribs.addAttribute(Params.UTM_SOURCE, utm_source);
		if (utm_medium != null)
			rattribs.addAttribute(Params.UTM_MEDIUM, utm_medium);
		if (utm_campaign != null)
			rattribs.addAttribute(Params.UTM_CAMPAIGN, utm_campaign);
		if (campaignname != null)
			rattribs.addAttribute(Params.UTM_CAMPAIGN_NAME, campaignname);
		if (adgroupname != null)
			rattribs.addAttribute(Params.UTM_AD_GROUP_NAME, adgroupname);
		if (utm_term != null)
			rattribs.addAttribute(Params.UTM_TERM, utm_term);
		if (keyword != null)
			rattribs.addAttribute(Params.KEYWORD, keyword);
		if (matchtype != null)
			rattribs.addAttribute(Params.MATCHTYPE, matchtype);
		if (network != null)
			rattribs.addAttribute(Params.NETWORK, network);
		if (device != null)
			rattribs.addAttribute(Params.DEVICE, device);
		if (creative != null)
			rattribs.addAttribute(Params.CREATIVE, creative);
	}

}
