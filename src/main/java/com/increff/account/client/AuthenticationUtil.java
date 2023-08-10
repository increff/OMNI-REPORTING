package com.increff.account.client;

import com.increff.account.model.QueryUserData;
import lombok.extern.log4j.Log4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j
public class AuthenticationUtil {

	public static UsernamePasswordAuthenticationToken getAuthenticationToken(QueryUserData qud) {
		// Create User Principal
		UserPrincipal principal = getPrincipal(qud);
		// Create and return Authentication token
		List<String> roles = new ArrayList<>();
		if (!CollectionUtils.isEmpty(qud.getResourceRoles())) {
			qud.getResourceRoles().forEach((a, b) -> {
				b.values().forEach(c -> {
					if (!StringUtils.isEmpty(c)) {
						roles.addAll(c);
					}
				});
			});
		}
		roles.addAll(principal.getRoles());
		List<GrantedAuthority> authorityList = getAuthorities(roles);
		UsernamePasswordAuthenticationToken token = //
				new UsernamePasswordAuthenticationToken(principal, null, authorityList);
		return token;
	}

	public static UsernamePasswordAuthenticationToken getAuthenticationToken(int id, String username, String app,
			String domain, List<String> roles) {
		UserPrincipal principal = getPrincipal(id, username, app, domain, roles);
		List<GrantedAuthority> authorityList = getAuthorities(roles);
		UsernamePasswordAuthenticationToken token = //
				new UsernamePasswordAuthenticationToken(principal, "", authorityList);
		return token;
	}

	public static List<GrantedAuthority> getAuthorities(List<String> roles) {
		List<GrantedAuthority> authorityList = new ArrayList<>();
		for (String name : roles) {
			//TODO remove this
			if (StringUtils.isEmpty(name)){
				continue;
			}
			SimpleGrantedAuthority authority = new SimpleGrantedAuthority(name);
			authorityList.add(authority);
		}
		return authorityList;
	}

	public static UserPrincipal getPrincipal(int id, String username, String app, String domain, List<String> roles) {
		UserPrincipal principal = new UserPrincipal();
		principal.setAppName(app);
		principal.setDomainName(domain);
		principal.setUsername(username);
		principal.setRoles(roles);
		principal.setId(id);
		return principal;
	}

	public static UserPrincipal getPrincipal(QueryUserData qud) {
		UserPrincipal principal = new UserPrincipal();
		principal.setAppName(qud.getAppName());
		principal.setDomainName(qud.getDomainName());
		principal.setDomainId(qud.getDomainId());
		principal.setEmail(qud.getEmail());
		principal.setId(qud.getId());
		principal.setFullName(qud.getFullName());
		principal.setResourceRoles(qud.getResourceRoles());
		principal.setRoles(qud.getRoles());
		principal.setUsername(qud.getUsername());
		principal.setCountry(qud.getCountry());
		principal.setOrgName(qud.getOrgName());
		principal.setPhone(qud.getPhone());
		return principal;
	}

	public static List<String> getRoles(Map<String, Map<String,List<String>>> resourceRoles, List<String> role) {
		List<String> roles = new ArrayList<>();
		for (String resource : resourceRoles.keySet()){
			resourceRoles.get(resource).values().forEach(r -> roles.addAll(r));
		}
		roles.addAll(role);
		return roles;
	}

}
