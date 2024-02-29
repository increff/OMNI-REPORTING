package com.increff.omni.reporting.util;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.form.ReportScheduleForm;

import java.util.*;

import static com.increff.omni.reporting.model.constants.Roles.USER_ACCESS_ADMIN_AUTHORITIES;


public class UserPrincipalUtil {

    public static Map<String, String> getCompleteMapWithAccessControl(Map<String, List<String>> params) {
        Map<String, String> finalMap = new HashMap<>(getStringToStringParamMap(params));
        finalMap.putAll(getAccessControlMap());
        return finalMap;
    }

    public static Map<String, String> getCompleteMapWithAccessControl(List<ReportScheduleForm.InputParamMap> paramMap) {
        Map<String, String> finalMap = new HashMap<>();
        paramMap.forEach(p -> {
            processParamMap(finalMap, p.getKey(), p.getValue(), p.getType());
        });
        finalMap.putAll(getAccessControlMap());
        return finalMap;
    }

    public static Map<String, String> getAccessControlMap() {
        Map<String, List<String>> accessControlMap = new HashMap<>();
        UserPrincipal principal = SecurityUtil.getPrincipal();
        List<String> accessRoles = principal.getRoles();
        accessRoles.retainAll(USER_ACCESS_ADMIN_AUTHORITIES);

        // If user has admin authorities, then do not set any param as query will have default value as column name
        // Which will make sure all values are selected
        if(!accessRoles.isEmpty())
            return getStringToStringParamMap(accessControlMap);
        Map<String, Map<String, List<String>>> resourceRoles = principal.getResourceRoles();
        accessControlMap.put(ResourceQueryParamKeys.fulfillmentLocationQueryParamKey
                , new ArrayList<>(Collections.singletonList("")));
        accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, new ArrayList<>(Collections.singletonList("")));
        accessControlMap.put(ResourceQueryParamKeys.restrictedResourceQueryParam, new ArrayList<>(Collections.singletonList("")));
        if(resourceRoles.containsKey(AppResourceKeys.fulfillmentLocationKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.fulfillmentLocationKey)
                    .keySet());
            accessControlMap.put(ResourceQueryParamKeys.fulfillmentLocationQueryParamKey, resourceValues);
        }
        if(resourceRoles.containsKey(AppResourceKeys.clientKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.clientKey).keySet());
            accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, resourceValues);
        }
        if(resourceRoles.containsKey(AppResourceKeys.restrictedResourceKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.restrictedResourceKey).keySet());
            accessControlMap.put(ResourceQueryParamKeys.restrictedResourceQueryParam, resourceValues);
        }
        return getStringToStringParamMap(accessControlMap);
    }

    public static Map<String, String> getStringToStringParamMap(Map<String, List<String>> params) {
        if(params.isEmpty())
            return new HashMap<>();
        Map<String, String> finalMap = new HashMap<>();
        for(Map.Entry<String, List<String>> entry : params.entrySet()) {
            processParamMap(finalMap, entry.getKey(), entry.getValue(), null);
        }
        return finalMap;
    }

    private static void processParamMap(Map<String, String> finalMap, String key, List<String> value,
                                        InputControlType type) {
        if(value.size() == 0) {
            finalMap.put(key, null);
            return;
        }
        List<String> fList = new ArrayList<>();
        for (String s : value) {
            if (s.equals("NULL") || s.equals("null") || (Objects.nonNull(type) &&
                    Arrays.asList(InputControlType.DATE_TIME, InputControlType.DATE).contains(type))) {
                fList.add(s);
                continue;
            }
            s = s.replace("'", "\\'");
            fList.add("'" + s + "'");
        }
        finalMap.put(key, String.join(",", fList));
    }


    public static boolean validateReportAppAccess(String appName) {
        Set<AppName> accessibleApps = getAccessibleApps();
        return accessibleApps.contains(AppName.valueOf(appName.toUpperCase()));
    }

    public static Set<AppName> getAccessibleApps() {
        Set<AppName> accessibleApps = new HashSet<>();
        List<String> userRoles = getPrincipal().getRoles();

        if(userRoles.contains(Roles.APP_ADMIN.getRole()) || userRoles.contains(Roles.REPORT_ADMIN.getRole())) {
            return new HashSet<>(Arrays.asList(AppName.values()));
        }

        for (String role : userRoles) {
            accessibleApps.add(AppName.valueOf(role.split("\\.")[0].toUpperCase()));
        }
        return accessibleApps;
    }

    public static UserPrincipal getPrincipal() {
        return SecurityUtil.getPrincipal();
    }
}
