package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.commons.StringUtil;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
//import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.JsonUtil;
//import com.nextscm.commons.spring.common.JsonUtil;

import java.util.List;

public abstract class AbstractValidator {

    public void add(List<InputControlType> validation) throws ApiException {
        // No validation required
    }

    // Parameter to be list of input controls with input
    public abstract void validate(List<String> displayName, List<String> paramValue, String reportName
            , Integer validationValue, ReportRequestType type) throws ApiException;

    public String getValidationMessage(String reportName, List<String> displayNames, ValidationType validationType
            , String extraMessage) {
        return reportName + " failed in validation for key / keys : " + JsonUtil.serialize(displayNames)
                + " , validation type : " + validationType + (!StringUtil.isEmpty(extraMessage) ? " message : "
                + extraMessage : extraMessage);
    }

    protected String getValueFromQuotes(String value) {
        if (StringUtil.isEmpty(value))
            return null;
        if (value.charAt(0) == '\'')
            return value.substring(1, value.length() - 1);
        return value;
    }
}
