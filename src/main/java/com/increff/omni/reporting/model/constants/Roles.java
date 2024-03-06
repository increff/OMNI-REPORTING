package com.increff.omni.reporting.model.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Roles {

    APP_ADMIN("app.admin"),
    REPORT_ADMIN("report.admin"),

    UNIFY_REPORT_STANDARD("unify.report.standard"),
    UNIFY_REPORT_CUSTOM("unify.report.custom"),

    OMNI_REPORT_STANDARD("unify.report.standard"),
    OMNI_REPORT_CUSTOM("unify.report.custom");

    private final String role;
    public static final List<String> USER_ACCESS_ADMIN_AUTHORITIES = Arrays.asList(
            Roles.APP_ADMIN.getRole(),
            Roles.REPORT_ADMIN.getRole());

    Roles(String role) {
        this.role = role;
    }

}
