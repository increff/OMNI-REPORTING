package com.increff.omni.reporting.util;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.AppResourceKeys;
import com.increff.omni.reporting.model.constants.ResourceQueryParamKeys;

import java.util.*;
import java.util.stream.Collectors;

public class UserPrincipalUtil {

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";
    private static final List<String> ADMIN_AUTHORITIES = Arrays.asList(APP_ADMIN, REPORT_ADMIN);

    public static Map<String, String> getCompleteMapWithAccessControl(Map<String, List<String>> params) {
        Map<String, String> finalMap = new HashMap<>(getStringToStringParamMap(params));
        finalMap.putAll(getAccessControlMap());
        return finalMap;
    }

    public static Map<String, String> getAccessControlMap() {
        Map<String, List<String>> accessControlMap = new HashMap<>();
        UserPrincipal principal = SecurityUtil.getPrincipal();
        List<String> accessRoles = principal.getRoles();
        accessRoles.retainAll(ADMIN_AUTHORITIES);
        // If user has admin authorities, then do not set any param as query will have default value as column name
        // Which will make sure all values are selected
        if(!accessRoles.isEmpty())
            return getStringToStringParamMap(accessControlMap);
        Map<String, Map<String, List<String>>> resourceRoles = principal.getResourceRoles();
        accessControlMap.put(ResourceQueryParamKeys.warehouseQueryParamKey, new ArrayList<>(Collections.singletonList("")));
        accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, new ArrayList<>(Collections.singletonList("")));
        if(resourceRoles.containsKey(AppResourceKeys.warehouseKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.warehouseKey).keySet());
            accessControlMap.put(ResourceQueryParamKeys.warehouseQueryParamKey, resourceValues);
        }
        if(resourceRoles.containsKey(AppResourceKeys.clientKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.clientKey).keySet());
            accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, resourceValues);
        }
        return getStringToStringParamMap(accessControlMap);
    }

    private static Map<String, String> getStringToStringParamMap(Map<String, List<String>> accessControlMap) {
        if(accessControlMap.isEmpty())
            return new HashMap<>();
        Map<String, String> finalMap = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : accessControlMap.entrySet()) {
            List<String> elements = entry.getValue();
            if(elements.size() == 0) {
                finalMap.put(entry.getKey(), null);
                continue;
            }
            elements = elements.stream().map(s -> "'" + s + "'").collect(Collectors.toList());
            finalMap.put(entry.getKey(), String.join(",", elements));
        }
        return finalMap;
    }
}
