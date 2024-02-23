package com.increff.omni.reporting.util;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.form.DashboardAddForm;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.increff.commons.springboot.server.DtoHelper.checkValid;

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
        if(forms.size() > MAX_DASHBOARD_CHARTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Maximum " + MAX_DASHBOARD_CHARTS + " charts allowed in a dashboard");
        for(DashboardChartForm form : forms) {
            checkValid(form);
        }
        validateChartAliasesAreUnique(forms);
    }

    private static void validateChartAliasesAreUnique(List<DashboardChartForm> forms) throws ApiException {
        Set<String> aliases = new HashSet<>();
        List<DashboardChartForm> duplicateAliases = forms.stream().filter(form -> !aliases.add(form.getChartAlias())).collect(Collectors.toList());
        if(duplicateAliases.size() > 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Same chart cannot be added twice. Duplicate chart alias: " + duplicateAliases.get(0).getChartAlias());
    }

    public static void validateDashboardAddForm(DashboardAddForm form) throws ApiException {
        checkValid(form);
        if (form.getCharts().size() == 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Atleast one chart is required in a dashboard");
        validateDashboardChartForms(form.getCharts());
    }

    public static void validateReportScheduleForm(ReportScheduleForm form) throws ApiException {
        checkValid(form);
        if(form.getSendTo().isEmpty() && form.getPipelineDetails().isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "Atleast one email or pipeline is required");
        if(form.getSendTo().size() > 0 && form.getPipelineDetails().size() > 0)
            throw new ApiException(ApiStatus.BAD_DATA, "Only one of email or pipeline should be given, not both");
    }
}
