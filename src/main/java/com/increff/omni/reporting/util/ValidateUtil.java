package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import com.increff.omni.reporting.model.form.ReportForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.util.List;

import static com.nextscm.commons.spring.server.DtoHelper.checkValid;

public class ValidateUtil {
    public static void validateReportForm(ReportForm reportForm) throws ApiException {
        checkValid(reportForm);
        if(reportForm.getChartType() != ChartType.TABLE && !reportForm.getIsChart())
            throw new ApiException(ApiStatus.BAD_DATA, "isChart should be true for Chart Type: " + reportForm.getChartType());

    }

    public static void validateDashboardChartForms(List<DashboardChartForm> forms) throws ApiException {
        int MAX_DASHBOARD_CHARTS = 10;
        if(forms.size() > MAX_DASHBOARD_CHARTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Maximum " + MAX_DASHBOARD_CHARTS + " charts allowed in a dashboard");
        for(DashboardChartForm form : forms){
            checkValid(form);
        }
    }
}
