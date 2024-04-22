package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleMandatoryValidator extends AbstractValidator {

    @Override
    public void validate(List<String> displayName, List<String> paramValue, String reportName,
                         Integer validationValue, ReportRequestType type)
            throws ApiException {
        List<String> nonEmptyValues = paramValue.stream().filter(p -> !StringUtil.isEmpty(getValueFromQuotes(p)))
                .collect(Collectors.toList());
        if (nonEmptyValues.isEmpty()) // If no value exists for any of the fields
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName
                    , ValidationType.SINGLE_MANDATORY, ""));
    }
}
