package com.increff.omni.reporting.model.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum Roles {

    REPORT_STANDARD("report.standard"), // USED IN ROLE OVERRIDE FILTER
    REPORT_CUSTOM("report.custom"),

    APP_ADMIN("app.admin"),
    REPORT_ADMIN("report.admin"),

    OMNI_REPORT_STANDARD("omni.report.standard"), // used to override report.standard role to omni.report.standard in filter
    OMNI_REPORT_CUSTOM("omni.report.custom"),

    ICC_REPORT_STANDARD("icc.report.standard"),
    ICC_REPORT_CUSTOM("icc.report.custom");

    private final String role;
    public static final List<String> USER_ACCESS_ADMIN_AUTHORITIES = Arrays.asList(
            Roles.APP_ADMIN.getRole(),
            Roles.REPORT_ADMIN.getRole());

    private final Map<String, Roles> roleList = new HashMap<>();

    Roles(String role) {
        this.role = role;
    }

}
