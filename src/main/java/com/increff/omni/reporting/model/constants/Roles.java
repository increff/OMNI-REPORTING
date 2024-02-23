package com.increff.omni.reporting.model.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Roles {

    APP_ADMIN("app.admin"),

    // REPORT_ADMIN("report.admin"), // todo : delete normal roles and only keep app.admin for schedules/dashabords
    // REPORT_STANDARD("report.standard"),
    // REPORT_CUSTOM("report.custom"),

    UNIFY_REPORT_ADMIN("unify.report.admin"), // todo change appname.report.admin to report.admin
    UNIFY_REPORT_STANDARD("unify.report.standard"),
    UNIFY_REPORT_CUSTOM("unify.report.custom"),

    CIMS_REPORT_ADMIN("cims.report.admin"),
    CIMS_REPORT_STANDARD("cims.report.standard"),
    CIMS_REPORT_CUSTOM("cims.report.custom"),

    OMS_REPORT_ADMIN("oms.report.admin"),
    OMS_REPORT_STANDARD("oms.report.standard"),
    OMS_REPORT_CUSTOM("oms.report.custom");

    private final String role;
    public static final List<String> USER_ACCESS_ADMIN_AUTHORITIES = Arrays.asList(Roles.APP_ADMIN.getRole(), Roles.CIMS_REPORT_ADMIN.getRole(), Roles.OMS_REPORT_ADMIN.getRole(), Roles.UNIFY_REPORT_ADMIN.getRole());

    Roles(String role) {
        this.role = role;
    }

}
