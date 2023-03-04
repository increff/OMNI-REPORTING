package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;

import java.time.ZonedDateTime;

public class ReportScheduleTestHelper {

    public static ReportSchedulePojo getReportSchedulePojo(String reportName, Boolean isEnabled, Boolean isDeleted,
                                                     Integer failureCount, Integer successCount,
                                                     ZonedDateTime nextRunTime, Integer userId, Integer orgId,
                                                           String cron) {
        ReportSchedulePojo schedulePojo = new ReportSchedulePojo();
        schedulePojo.setReportName(reportName);
        schedulePojo.setIsEnabled(isEnabled);
        schedulePojo.setIsDeleted(isDeleted);
        schedulePojo.setFailureCount(failureCount);
        schedulePojo.setSuccessCount(successCount);
        schedulePojo.setNextRuntime(nextRunTime);
        schedulePojo.setUserId(userId);
        schedulePojo.setOrgId(orgId);
        schedulePojo.setCron(cron);
        return schedulePojo;
    }

    public static ReportScheduleInputParamsPojo getReportScheduleInputParamsPojo(Integer scheduleId, String paramKey,
                                                                                 String paramValue, String displayValue) {
        ReportScheduleInputParamsPojo inputParamsPojo = new ReportScheduleInputParamsPojo();
        inputParamsPojo.setScheduleId(scheduleId);
        inputParamsPojo.setParamKey(paramKey);
        inputParamsPojo.setParamValue(paramValue);
        inputParamsPojo.setDisplayValue(displayValue);
        return inputParamsPojo;
    }
}
