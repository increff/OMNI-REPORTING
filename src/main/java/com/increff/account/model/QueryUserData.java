package com.increff.account.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryUserData implements Serializable {

	private boolean status;
	private String message;

	// About the user
	private int id;
	private String username;
	private String email;
	private String fullName;
	private String domainName;
	private String appName;
	private int domainId;
	private String country;

	private List<String> roles;
	private String authMode;
	private String phone;
	private String orgName;
	private Map<String, Map<String,List<String>>> resourceRoles;
}
