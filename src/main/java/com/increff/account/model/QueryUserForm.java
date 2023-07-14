package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueryUserForm {

	// To be used only when providing both username & password
	private String domainName;
	private String username;
	// Provide both username & password, or only token
	private String credential;
	// To be used while creating tokens
	private int duration;
	private String uuid;
	private String value;
	private String resourceType;
	private String resourceValue;

}
