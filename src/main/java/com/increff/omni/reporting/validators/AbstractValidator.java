package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.increff.commons.springboot.common.ApiException;
import org.apache.commons.lang3.StringUtils;

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
        StringBuilder err = new StringBuilder();

        if (!StringUtils.isEmpty(extraMessage)) err.append(extraMessage);
        err.append("\n").append("Filter(s) : ");

        err.append(getDisplayNamesErrorString(displayNames));

        err.append("\nValidation Type : ").append(validationType);
        err.append("\nReport : ").append(reportName);

        return err.toString();
    }

    protected static String getDisplayNamesErrorString(List<String> displayNames) {
        StringBuilder err = new StringBuilder();
        err.append("(");
        for (String displayName : displayNames) {
            if (StringUtil.isEmpty(displayName))
                continue;
            err.append(displayName).append(", ");
        }
        if (!displayNames.isEmpty()) // remove last comma
            err.delete(err.length() - 2, err.length());
        err.append(")");
        return err.toString();
    }

    protected String getValueFromQuotes(String value) {
        if (StringUtil.isEmpty(value))
            return null;
        if (value.charAt(0) == '\'')
            return value.substring(1, value.length() - 1);
        return value;
    }
}
