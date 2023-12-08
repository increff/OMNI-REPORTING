package com.increff.omni.reporting.util;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.AppResourceKeys;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ResourceQueryParamKeys;
import com.increff.omni.reporting.model.form.ReportScheduleForm;

import java.util.*;

public class UserPrincipalUtil {

    private static final String APP_ADMIN = "app.admin";
    private static final String REPORT_ADMIN = "report.admin";
    private static final List<String> ADMIN_AUTHORITIES = Arrays.asList(APP_ADMIN, REPORT_ADMIN);
    private static final String SQL_FUNC_PREFIX = "SQLF#";

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
        accessRoles.retainAll(ADMIN_AUTHORITIES);
        // If user has admin authorities, then do not set any param as query will have default value as column name
        // Which will make sure all values are selected
        if(!accessRoles.isEmpty())
            return getStringToStringParamMap(accessControlMap);
        Map<String, Map<String, List<String>>> resourceRoles = principal.getResourceRoles();
        accessControlMap.put(ResourceQueryParamKeys.fulfillmentLocationQueryParamKey
                , new ArrayList<>(Collections.singletonList("")));
        accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, new ArrayList<>(Collections.singletonList("")));
        if(resourceRoles.containsKey(AppResourceKeys.fulfillmentLocationKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.fulfillmentLocationKey)
                    .keySet());
            accessControlMap.put(ResourceQueryParamKeys.fulfillmentLocationQueryParamKey, resourceValues);
        }
        if(resourceRoles.containsKey(AppResourceKeys.clientKey)) {
            List<String> resourceValues = new ArrayList<>(resourceRoles.get(AppResourceKeys.clientKey).keySet());
            accessControlMap.put(ResourceQueryParamKeys.clientQueryParam, resourceValues);
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
            if (s.equals("NULL") || s.equals("null") || isSqlFunc(key) || (Objects.nonNull(type) &&
                    Arrays.asList(InputControlType.DATE_TIME, InputControlType.DATE).contains(type))) {
                fList.add(s);
                continue;
            }
            s = s.replace("'", "\\'");
            fList.add("'" + s + "'");
        }
        finalMap.put(key, String.join(",", fList));
    }

    public static boolean isSqlFunc(String s) {
        return s.toUpperCase().startsWith(SQL_FUNC_PREFIX);
    }
}
