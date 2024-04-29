package com.increff.omni.reporting.validators;

import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DateValidator extends AbstractValidator {

    @Override
    public void add(List<InputControlType> inputControlTypeList) throws ApiException {
        // Check all are DATE input controls
        if (inputControlTypeList.stream()
                .anyMatch(c -> !Arrays.asList(InputControlType.DATE, InputControlType.DATE_TIME).contains(c)))
            throw new ApiException(ApiStatus.BAD_DATA,
                    "DATE_RANGE validation can only be applied on DATE / DATE_TIME " +
                            "input controls");
        if (inputControlTypeList.size() != 2)
            throw new ApiException(ApiStatus.BAD_DATA, "DATE_RANGE validation type should have exactly 2 DATE / " +
                    "DATE_TIME input controls");
    }

    @Override
    public void validate(List<String> displayName, List<String> paramValue, String reportName,
                         Integer validationValue, ReportRequestType type)
            throws ApiException {
        if(type.equals(ReportRequestType.EMAIL))
            return;
        if (paramValue.size() != 2)
            throw new ApiException(ApiStatus.BAD_DATA, "Exactly 2 Date inputs are required to validate");
        List<String> nonEmptyValues = paramValue.stream().filter(p -> !StringUtil.isEmpty(getValueFromQuotes(p)))
                .collect(Collectors.toList());
        if(nonEmptyValues.isEmpty())
            return;
        if (nonEmptyValues.size() != 2)
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Both from and to date should be selected for filters : " + getDisplayNamesErrorString(displayName));
        try {
            // We can't define which one is exactly the start date
            ZonedDateTime date1 = ZonedDateTime.parse(getValueFromQuotes(paramValue.get(0))
                    , DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN));
            ZonedDateTime date2 = ZonedDateTime.parse(getValueFromQuotes(paramValue.get(1))
                    , DateTimeFormatter.ofPattern(CommonDtoHelper.TIME_ZONE_PATTERN));
            if (date1.isBefore(date2)) {
                if (date2.minusDays(validationValue).isAfter(date1))
                    throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName
                            , ValidationType.DATE_RANGE, "Date range crossed " + validationValue + " days"));
            } else if (date2.isBefore(date1)) {
                if (date1.minusDays(validationValue).isAfter(date2))
                    throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName
                            , ValidationType.DATE_RANGE, "Date range crossed " + validationValue + " days"));
            } else
                throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName
                        , ValidationType.DATE_RANGE, "Both dates can't be equal"));
        } catch (DateTimeParseException e) {
            throw new ApiException(ApiStatus.BAD_DATA, getValidationMessage(reportName, displayName
                    , ValidationType.DATE_RANGE, "Date is not in correct format : "
                            + JsonUtil.serialize(paramValue)));
        }
    }
}
