package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.pojo.CustomReportAccessPojo;

public class CustomReportAccessTestHelper {

    public static CustomReportAccessPojo getCustomReportAccessPojo(Integer orgId, Integer reportId) {
        CustomReportAccessPojo pojo = new CustomReportAccessPojo();
        pojo.setOrgId(orgId);
        pojo.setReportId(reportId);
        return pojo;
    }
}
