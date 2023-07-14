package com.increff.account.client;

import com.nextscm.commons.spring.client.AppClientException;

public class AuthException extends Exception {

	private static final long serialVersionUID = 1L;

	public AuthException(String message) {
		super(message);
	}

	public AuthException(String message, AppClientException e) {
		super(message, e);
	}
}
