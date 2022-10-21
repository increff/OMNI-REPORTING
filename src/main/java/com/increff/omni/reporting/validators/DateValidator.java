package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DateValidator extends AbstractValidator {

    @Override
    public void add(List<InputControlType> inputControlTypeList) throws ApiException {
        // Check all are DATE input controls
        if (inputControlTypeList.stream().anyMatch(c -> !c.equals(InputControlType.DATE)))
            throw new ApiException(ApiStatus.BAD_DATA, "DATE_RANGE validation can only be applied on DATE input controls");
        if (inputControlTypeList.size() != 2)
            throw new ApiException(ApiStatus.BAD_DATA, "DATE_RANGE validation type should have exactly 2 DATE input controls");
    }

    @Override
    public void validate(List<String> displayName, List<String> paramValue, String reportName, Integer validationValue) throws ApiException {
        List<String> nonEmptyValues = paramValue.stream().filter(p -> !StringUtil.isEmpty(p)).collect(Collectors.toList());
        if(nonEmptyValues.isEmpty())
            return;
        if(nonEmptyValues.size()!=2)
            throw new ApiException(ApiStatus.BAD_DATA, "Exactly 2 Date inputs are required to validate");
        try {
            // We can't define which one is exactly the start date
            ZonedDateTime date1 = ZonedDateTime.parse(getValueFromQuotes(paramValue.get(0))
                    , DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN));
            ZonedDateTime date2 = ZonedDateTime.parse(getValueFromQuotes(paramValue.get(1))
                    , DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN));
            if(date1.isBefore(date2)) {
                if(date2.minusDays(validationValue).isAfter(date1))
                    throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.DATE_RANGE, "Date range crossed " + validationValue + " days"));
            }
            else if(date2.isBefore(date1)) {
                if(date1.minusDays(validationValue).isAfter(date2))
                    throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.DATE_RANGE, "Date range crossed " + validationValue + " days"));
            } else
                throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.DATE_RANGE, "Both dates can't be equal"));
        } catch (DateTimeParseException e) {
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName, ValidationType.DATE_RANGE, "Date is not in correct format : " + paramValue));
        }
    }

    private String getValueFromQuotes(String value) {
        return value.substring(1, value.length()-1);
    }
}
