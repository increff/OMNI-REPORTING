package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlowApiHelper {

    public static ReportControlsPojo getReportControlPojo(Integer reportId, Integer controlId) {
        ReportControlsPojo pojo = new ReportControlsPojo();
        pojo.setReportId(reportId);
        pojo.setControlId(controlId);
        return pojo;
    }

    public static List<ReportValidationGroupPojo> getValidationGroupPojoList(ValidationGroupForm groupForm, Integer reportId) {
        List<ReportValidationGroupPojo> groupPojoList = new ArrayList<>();
        groupForm.getReportControlIds().forEach(c -> {
            ReportValidationGroupPojo pojo = new ReportValidationGroupPojo();
            pojo.setGroupName(groupForm.getGroupName());
            pojo.setReportId(reportId);
            pojo.setReportControlId(c);
            pojo.setType(groupForm.getValidationType());
            pojo.setValidationValue(groupForm.getValidationValue());
            groupPojoList.add(pojo);
        });
        return groupPojoList;
    }
}
