package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.form.ReportForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import static com.nextscm.commons.spring.server.DtoHelper.checkValid;

public class ValidateUtil {

    public static void validateReportForm(ReportForm reportForm) throws ApiException {
        checkValid(reportForm);
        if(reportForm.getChartType() != ChartType.REPORT && !reportForm.getIsDashboard())
            throw new ApiException(ApiStatus.BAD_DATA, "Chart Types are only allowed for dashboard reports");

    }
}
