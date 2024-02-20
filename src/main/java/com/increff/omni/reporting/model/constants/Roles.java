package com.increff.omni.reporting.model.constants;

import java.util.Arrays;
import java.util.List;

public enum Roles {
    APP_ADMIN("app.admin"),
    REPORT_ADMIN("report.admin"),
    REPORT_STANDARD("report.standard"),
    REPORT_CUSTOM("report.custom"),

    CIMS_REPORT_ADMIN("cims.report.admin"),
    CIMS_REPORT_STANDARD("cims.report.standard"),
    CIMS_REPORT_CUSTOM("cims.report.custom");

    private final String role;
    public static final List<String> USER_ACCESS_ADMIN_AUTHORITIES = Arrays.asList(Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole());

    Roles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
