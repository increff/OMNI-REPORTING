package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.ValidationModel;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Component
public class DateValidator extends AbstractValidator{

    @Override
    public void add(ValidationModel validation) {

    }

    @Override
    public void validate(String displayName, String paramValue, String reportName) throws ApiException {
        super.validate(displayName, paramValue, reportName);
        try {
            ZonedDateTime.parse(paramValue);
        } catch (DateTimeParseException e) {
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.DATE, "Date is not in correct format : " + paramValue));
        }
    }
}
