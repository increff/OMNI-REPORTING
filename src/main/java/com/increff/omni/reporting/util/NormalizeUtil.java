package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.DynamicDate;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.form.ReportScheduleForm;

import java.util.Arrays;
import java.util.Objects;

import static com.increff.omni.reporting.util.ConstantsUtil.USER_TIMEZONE;

public class NormalizeUtil {

    public static void normalizeReportScheduleForm(ReportScheduleForm form) {
        if (Objects.isNull(form.getParamMap()))
            return;

        for (ReportScheduleForm.InputParamMap param : form.getParamMap()) {
            if (!(param.getType().equals(InputControlType.DATE)
                    || param.getType().equals(InputControlType.DATE_TIME)))
                continue;
            if (param.getValue().isEmpty())
                continue;

            String value = param.getValue().get(0);
            value = DynamicDate.enumToQuery(value);
            value = value.replace(USER_TIMEZONE, "\"" + form.getTimezone() + "\"");
            param.setValue(Arrays.asList(value));
        }

    }


}
