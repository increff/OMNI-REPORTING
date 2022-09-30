package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.ValidationModel;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

public abstract class AbstractValidator {

    public abstract void add(ValidationModel validation);

    //Parameter to be list of input controls with input
    public void validate(String displayName, String paramValue, String reportName) throws ApiException {
        if(StringUtil.isEmpty(paramValue))
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.MANDATORY, ""));
    }

    public String getValidationMessage(String reportName, String displayName, ValidationType validationType, String extraMessage) {
        return reportName + " failed in validation for key : " +displayName + " , validation type : " + validationType
                + (!StringUtil.isEmpty(extraMessage) ? " message : " + extraMessage : extraMessage);
    }
}
