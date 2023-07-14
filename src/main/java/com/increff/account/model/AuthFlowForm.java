package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthFlowForm {

	// To be used only when providing both username & password
	private String domain;
	// Provide both username & password, or only token
	private String nextUrl;
	// To be used while creating tokens
	private int duration;

}
