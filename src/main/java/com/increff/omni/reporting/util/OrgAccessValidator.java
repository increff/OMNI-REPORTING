package com.increff.omni.reporting.util;

import com.increff.account.client.UserPrincipal;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.api.OrganizationApi;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;



@Log4j2
@Component
public class OrgAccessValidator {

    @Autowired
    private OrganizationApi organizationApi;
    private final ConcurrentHashMap<String, CachedOrg> orgCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(5);

   
    private static class CachedOrg {
        final OrganizationPojo org;
        final long expiresAt;

        CachedOrg(OrganizationPojo org) {
            this.org = org;
            this.expiresAt = System.currentTimeMillis() + CACHE_TTL_MS;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private OrganizationPojo getOrgByNameCached(String orgName) {
        CachedOrg cached = orgCache.get(orgName);
        if (cached != null && !cached.isExpired()) {
            log.debug("Cache HIT for org: {}", orgName);
            return cached.org;
        }
        log.debug("Cache MISS for org: {} - fetching from database", orgName);
        OrganizationPojo org = organizationApi.getByName(orgName);

        if (org != null) {
            orgCache.put(orgName, new CachedOrg(org));
        }

        return org;
    }

   
    public void validateOrgAccess(Integer requestedOrgId) throws ApiException {
        UserPrincipal principal = UserPrincipalUtil.getPrincipal();
        String jwtOrgName = principal.getFullName();
        Integer jwtOrgId = getOrgIdFromToken();

        if (!jwtOrgId.equals(requestedOrgId)) {
            log.warn("Organization access denied - JWT org: {} (id: {}), Requested org: {}",
                    jwtOrgName, jwtOrgId, requestedOrgId);
            throw new ApiException(ApiStatus.BAD_DATA,
                String.format("Forbidden: You don't have access to organization ID %d. Your token is scoped to organization '%s' (ID: %d)",
                        requestedOrgId, jwtOrgName, jwtOrgId)
            );
        }

        log.debug("Organization access validated - user: {}, org: {} (id: {})",
                principal.getUsername(), jwtOrgName, jwtOrgId);
    }

    public Integer getOrgIdFromToken() throws ApiException {
        UserPrincipal principal = UserPrincipalUtil.getPrincipal();
        String jwtOrgName = principal.getFullName();

        if (jwtOrgName == null || jwtOrgName.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "Unauthorized: No organization specified in token");
        }

        OrganizationPojo org = getOrgByNameCached(jwtOrgName);
        if (org == null) {
            throw new ApiException(ApiStatus.BAD_DATA, "Unauthorized: Invalid organization in token");
        }

        return org.getId();
    }

}