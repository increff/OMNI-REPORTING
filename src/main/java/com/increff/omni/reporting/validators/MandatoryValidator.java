package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MandatoryValidator extends AbstractValidator {

    @Override
    public void validate(List<String> displayNames, List<String> paramValues, String reportName, Integer validationValue) throws ApiException {
        for(String p : paramValues){
            if(StringUtil.isEmpty(p))
                throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayNames, ValidationType.MANDATORY, ""));
        }
    }
}
