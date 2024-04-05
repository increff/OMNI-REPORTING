package com.increff.omni.reporting.util;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.Roles;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.nextscm.commons.spring.common.JsonUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.model.constants.Roles.USER_ACCESS_ADMIN_AUTHORITIES;

@Log4j
public class UserPrincipalUtil {

    private static final String USER_ACCESS_QUERY_PARAM_PREFIX = "user.access.";
    // token till which the query param key is to be extracted
    private static final String USER_ACCESS_QUERY_PARAM_SUBSTRING_CLOSE = ",";


    public static Map<String, String> getMapWithoutAccessControl(Map<String, List<String>> params) {
        Map<String, String> finalMap = new HashMap<>(getStringToStringParamMap(params));
        // finalMap.putAll(getAccessControlMap()); // Access Controlled Filters are validated internally when validating filter which has a query associated with it
        return finalMap;
    }

    public static Map<String, String> getMapWithoutAccessControl(List<ReportScheduleForm.InputParamMap> paramMap) {
        Map<String, String> finalMap = new HashMap<>();
        paramMap.forEach(p -> {
            processParamMap(finalMap, p.getKey(), p.getValue(), p.getType());
        });
        // finalMap.putAll(getAccessControlMap()); Schedulers are run by app.admin who will have all access. Thus, no need to add user.access values to the map
        return finalMap;
    }

    /**
     * For Standard Users
     * Returns a map containing all query param keys starting with 'user.access.'
     * and values as empty string or actual value account server values set up for the user
     * <p>
     * For Admin Users
     * Returns empty map. Values of 'user.access.' keys will be null
     *
     * @param query Report Query
     * @return Map<String, String> containing values for all query param keys starting with 'user.access.'
     */
    public static Map<String, String> getAccessControlMapForUserAccessQueryParamKeys(String query) {
        Map<String, List<String>> accessControlMap = new HashMap<>();
        UserPrincipal principal = SecurityUtil.getPrincipal();
        List<String> accessRoles = principal.getRoles();
        accessRoles.retainAll(USER_ACCESS_ADMIN_AUTHORITIES);

        // If user has admin authorities, then do not set any param as query will have default value as column name
        // Which will make sure all values are selected
        if(!accessRoles.isEmpty())
            return getStringToStringParamMap(accessControlMap);


        accessControlMap = addResourceValuesForAccessQueryParamKeys(query, principal);
        return getStringToStringParamMap(accessControlMap);
    }

    private static Map<String, List<String>> addResourceValuesForAccessQueryParamKeys(String query, UserPrincipal principal) {
        Map<String, List<String>> accessControlMap = new HashMap<>();

        String[] queryParamKeys = StringUtils.substringsBetween(query, USER_ACCESS_QUERY_PARAM_PREFIX, USER_ACCESS_QUERY_PARAM_SUBSTRING_CLOSE);
        cleanQueryParamKeys(queryParamKeys);

        Map<String, Map<String, List<String>>> resourceRoles = principal.getResourceRoles();
        for (String queryParamKey : queryParamKeys) {
            String queryParamFullKey = USER_ACCESS_QUERY_PARAM_PREFIX + queryParamKey;

            // Set default value as empty string (No Access if values are not set for user)
            accessControlMap.put(queryParamFullKey, new ArrayList<>(Collections.singletonList("")));

            // If user has access to the resource, then set the values for the query param key
            // Query Param Key and Resource Key In Account Server should be same
            if (resourceRoles.containsKey(queryParamKey)) {
                List<String> resourceValues = new ArrayList<>(resourceRoles.get(queryParamKey).keySet());
                accessControlMap.put(queryParamFullKey, resourceValues);
            }
        }

        log.debug("Access Control Map: " + JsonUtil.serialize(accessControlMap) +
                " email: " + (((Objects.nonNull(principal.getEmail()))) ? principal.getEmail() : "null") +
                " username: " + (((Objects.nonNull(principal.getUsername())) ? principal.getUsername() : "null")));
        return accessControlMap;
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

    public static Set<AppName> getAccessibleApps() {
        Set<AppName> accessibleApps = new HashSet<>();
        List<String> userRoles = getPrincipal().getRoles();
        log.debug("User roles: " + userRoles);

        if(userRoles.contains(Roles.APP_ADMIN.getRole()) || userRoles.contains(Roles.REPORT_ADMIN.getRole())) {
            return new HashSet<>(Arrays.asList(AppName.values()));
        }

        userRoles = userRoles.stream().filter(role -> role.contains(Roles.REPORT_STANDARD.getRole()) || role.contains(Roles.REPORT_CUSTOM.getRole())).collect(Collectors.toList());
        for (String role : userRoles) {
            String app =  "";
            if(role.split("\\.").length > 0)
                app = role.split("\\.")[0].toUpperCase();

            if(app.isEmpty() || role.equalsIgnoreCase(Roles.REPORT_STANDARD.getRole()) || role.equalsIgnoreCase(Roles.REPORT_CUSTOM.getRole()))
                continue; // Skip standard role as it is not an app! User should always have role omni.report.standard/custom along with report.standard/custom!!
            log.debug("Adding app: " + app);
            accessibleApps.add(AppName.valueOf(app));
        }
        return accessibleApps;
    }

    public static UserPrincipal getPrincipal() {
        return SecurityUtil.getPrincipal();
    }


    private static void cleanQueryParamKeys(String[] queryParamKeys) {
        log.debug("Query Param Keys: " + Arrays.toString(queryParamKeys));
        for (int i = 0; i < queryParamKeys.length; i++) {
            queryParamKeys[i] = queryParamKeys[i].split(",")[0].trim();
        }
        log.debug("Query Param Keys (Cleaned): " + Arrays.toString(queryParamKeys));
    }
}
