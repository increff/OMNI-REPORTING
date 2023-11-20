package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.form.DashboardAddForm;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import com.increff.omni.reporting.model.form.ReportForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.List;
import java.util.Objects;

import static com.nextscm.commons.spring.server.DtoHelper.checkValid;

public class ValidateUtil {
    public static int MAX_DASHBOARD_CHARTS = 6;
    public static void validateReportForm(ReportForm form) throws ApiException {
        checkValid(form);
        if(form.getIsChart() && form.getCanSchedule())
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard Reports can't be scheduled");
        if(!form.getCanSchedule() && Objects.nonNull(form.getMinFrequencyAllowedSeconds()))
            throw new ApiException(ApiStatus.BAD_DATA, "Min Frequency Allowed Seconds " + form.getMinFrequencyAllowedSeconds() + " should be null for non-scheduled reports");

        if(Objects.nonNull(form.getChartType().getLEGENDS_COUNT_VALIDATION()) && form.getLegends().size() != form.getChartType().getLEGENDS_COUNT_VALIDATION())
            throw new ApiException(ApiStatus.BAD_DATA, "Invalid legend count. Expected: " + form.getChartType().getLEGENDS_COUNT_VALIDATION() + " Actual: " + form.getLegends().size());
        if(form.getChartType() != ChartType.REPORT && !form.getIsChart())
            throw new ApiException(ApiStatus.BAD_DATA, "isChart should be true for Chart Type: " + form.getChartType());

    }

    public static void validateDashboardChartForms(List<DashboardChartForm> forms) throws ApiException {
        //todo move this to property file and use default value and confirm value - cannot autowire application properties and use it in static function
        if(forms.size() > MAX_DASHBOARD_CHARTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Maximum " + MAX_DASHBOARD_CHARTS + " charts allowed in a dashboard");
        for(DashboardChartForm form : forms) {
            checkValid(form);
        }
    }

    public static void validateDashboardAddForm(DashboardAddForm form) throws ApiException {
        checkValid(form);
        if (form.getCharts().size() == 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Atleast one chart is required in a dashboard");
        validateDashboardChartForms(form.getCharts());
    }
}
