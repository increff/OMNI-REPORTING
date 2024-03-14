package com.increff.omni.reporting.model.constants;

import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.Getter;

import java.util.*;

@Getter
public enum Roles {

    REPORT_STANDARD("report.standard"), // USED IN ROLE OVERRIDE FILTER
    REPORT_CUSTOM("report.custom"),

    APP_ADMIN("app.admin"),
    REPORT_ADMIN("report.admin"),

    UNIFY_REPORT_STANDARD("unify.report.standard"),
    UNIFY_REPORT_CUSTOM("unify.report.custom"),

    OMNI_REPORT_STANDARD("omni.report.standard"),
    OMNI_REPORT_CUSTOM("omni.report.custom");




    private final String role;
    public static final List<String> USER_ACCESS_ADMIN_AUTHORITIES = Arrays.asList(
            Roles.APP_ADMIN.getRole(),
            Roles.REPORT_ADMIN.getRole());

    private final Map<String, Roles> roleList = new HashMap<>();

    Roles(String role) {
        this.role = role;
    }

    public static Roles getRoleByString(String role) throws ApiException {
        for (Roles r : Roles.values()) {
            if (r.getRole().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new ApiException(ApiStatus.BAD_DATA, "Invalid role: " + role);
    }


}
