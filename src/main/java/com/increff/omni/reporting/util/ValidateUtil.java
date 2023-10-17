package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.form.ReportForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.Objects;

public class ValidateUtil {

    public static void validateForm(ReportForm form) throws ApiException {
        if(form.getIsDashboard() && form.getCanSchedule())
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard Reports can't be scheduled");
        if(!form.getCanSchedule() && Objects.nonNull(form.getMinFrequencyAllowedSeconds()))
            throw new ApiException(ApiStatus.BAD_DATA, "Min Frequency Allowed Seconds " + form.getMinFrequencyAllowedSeconds() + " should be null for non-scheduled reports");
    }
}
