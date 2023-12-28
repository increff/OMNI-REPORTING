package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;

import java.util.Objects;

import static com.increff.commons.springboot.server.DtoHelper.checkValid;

public class ValidateUtil {

    public static void validateForm(ReportForm form) throws ApiException {
        checkValid(form);
        if(form.getIsDashboard() && form.getCanSchedule())
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard Reports can't be scheduled");
        if(!form.getCanSchedule() && Objects.nonNull(form.getMinFrequencyAllowedSeconds()))
            throw new ApiException(ApiStatus.BAD_DATA, "Min Frequency Allowed Seconds " + form.getMinFrequencyAllowedSeconds() + " should be null for non-scheduled reports");
    }
}
