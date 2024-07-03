package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MandatoryValidator extends AbstractValidator {

    @Override
    public void validate(List<String> displayNames, List<String> paramValues, String reportName,
                         Integer validationValue, ReportRequestType type)
            throws ApiException {
        for(String p : paramValues){
            if(StringUtil.isEmpty(getValueFromQuotes(p)))
                throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayNames
                        , ValidationType.MANDATORY, "Value should be present for this filter"));
        }
    }
}
