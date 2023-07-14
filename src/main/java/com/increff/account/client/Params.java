package com.increff.account.client;

public class Params {

	public static final String APP_REFERRER = "referrer";

	public static final String UTM_SOURCE = "utm_source";
	public static final String UTM_MEDIUM = "utm_medium";
	public static final String UTM_CAMPAIGN = "utm_campaign";
	public static final String UTM_CAMPAIGN_NAME = "campaignname";
	public static final String UTM_AD_GROUP_NAME = "adgroupname";
	public static final String UTM_TERM = "utm_term";
	public static final String KEYWORD = "keyword";
	public static final String MATCHTYPE = "matchtype";
	public static final String NETWORK = "network";
	public static final String DEVICE = "device";
	public static final String CREATIVE = "creative";

	/// session/login-post redirects to this URL after permanent token is generated
	public static final String APP_NEXT_URL = "appNextUrl";

	public static final String APP_NAME = "appName";
	public static final String DOMAIN_NAME = "domainName";
	// Auth server redirects to this URL after login is complete.
	// This URL has to be https://x.y.z/context_path/session/login-post
	public static final String AUTH_NEXT_URL = "authNextUrl";
	public static final String AUTH_TOKEN = "authToken";
	public static final String AUTH_TEMP_TOKEN = "authTempToken";
	public static final String CHANGE_PASS_TOKEN = "changePassToken";
	public static final String AUTH_APP_TOKEN = "authAppToken";
	public static final String AUTH_STATUS = "authStatus";
	public static final String AUTH_MESSAGE = "authMessage";
	// Auth server redirects to this URL after registration is complete
	public static final String REGISTER_NEXT_URL = "registerNextUrl";
	public static final String CAPTCHA_TYPE = "captcha_type";
	public static final String USERS = "users";
	public static final String INSTANCE_NAME = "account-hazel-instance";
}
