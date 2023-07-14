package com.increff.account.client;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class CookieUtil {

	public static String getCookie(HttpServletRequest req, String name) {
		List<Cookie> cookieList = getCookieFromRequest(req, name);
		if (cookieList == null || cookieList.size() == 0){
			return null;
		}
		return cookieList.get(0).getValue();
	}

	public static List<Cookie> getCookieFromRequest(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			return null;
		}
		List<Cookie> cookieList = new ArrayList<>();
		for (Cookie c : cookies) {
			if (name.equals(c.getName())) {
				cookieList.add(c);
			}
		}

		return cookieList;
	}
}
