package com.increff.account.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO add Resource role key
public class UserPrincipal {

	private int id;
	private String email;
	private String fullName;
	private String username;
	private String domainName;
	private String appName;
	private int domainId;
	private String country;

	private String phone;

	private String orgName;
	
	private List<String> roles;
	private Map<String, Map<String,List<String>>> resourceRoles = new HashMap<>();

	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public List<String> getRoles() {
		return roles;
	}

	public Map<String,Map<String,List<String>>> getResourceRoles() {
		return this.resourceRoles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public void setResourceRoles(Map<String,Map<String,List<String>>> resourceRoles) {
		this.resourceRoles = resourceRoles;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	@Override
	public String toString() {
		return "{ id: " + id + ", domain: " + domainName + ", username: " + username + "}";
	}

}