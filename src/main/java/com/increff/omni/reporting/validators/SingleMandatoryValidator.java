package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.ValidationModel;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.stereotype.Component;

@Component
public class SingleMandatoryValidator extends AbstractValidator{
    @Override
    public void add(ValidationModel validation) {

    }

    @Override
    public void validate(String displayName, String paramValue, String reportName) throws ApiException {
        super.validate(displayName, paramValue, reportName);
        String[] values = paramValue.split(",");
        if(values.length != 1)
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.SINGLE_MANDATORY, ""));
    }
}
