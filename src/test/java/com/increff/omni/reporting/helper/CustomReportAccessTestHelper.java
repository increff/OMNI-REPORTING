package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.CustomReportAccessForm;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;

public class CustomReportAccessTestHelper {

    public static CustomReportAccessPojo getCustomReportAccessPojo(Integer orgId, Integer reportId) {
        CustomReportAccessPojo pojo = new CustomReportAccessPojo();
        pojo.setOrgId(orgId);
        pojo.setReportId(reportId);
        return pojo;
    }

    public static CustomReportAccessForm getCustomReportAccessForm(Integer reportId, Integer orgId) {
        CustomReportAccessForm form = new CustomReportAccessForm();
        form.setReportId(reportId);
        form.setOrgId(orgId);
        return form;
    }
}
