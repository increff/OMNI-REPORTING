package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.form.CronScheduleForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static ReportScheduleForm getReportScheduleForm(String minute, String hour, String dayOfMonth, String reportName
            , String timezone, Boolean isEnabled, List<String> sendTo, List<ReportScheduleForm.InputParamMap> inputParamMaps) {
        ReportScheduleForm form = new ReportScheduleForm();
        CronScheduleForm cronScheduleForm = new CronScheduleForm();
        cronScheduleForm.setMinute(minute);
        cronScheduleForm.setHour(hour);
        cronScheduleForm.setDayOfMonth(dayOfMonth);
        form.setCronSchedule(cronScheduleForm);
        form.setReportName(reportName);
        form.setTimezone(timezone);
        form.setIsEnabled(isEnabled);
        form.setSendTo(sendTo);
        form.setParamMap(inputParamMaps);
        return form;
    }

    public static List<ReportScheduleForm.InputParamMap> getInputParamList() {
        List<ReportScheduleForm.InputParamMap> inputParamMapList = new ArrayList<>();
        ReportScheduleForm.InputParamMap inputParamMap = new ReportScheduleForm.InputParamMap();
        inputParamMap.setKey("clientId");
        inputParamMap.setValue(List.of("1100002253"));
        inputParamMap.setType(InputControlType.MULTI_SELECT);
        inputParamMapList.add(inputParamMap);
        return inputParamMapList;
    }

    public static List<ReportScheduleEmailsPojo> getEmailsPojo(Integer scheduleId) {
        List<ReportScheduleEmailsPojo> emailsPojoList = new ArrayList<>();
        ReportScheduleEmailsPojo scheduleEmailsPojo = new ReportScheduleEmailsPojo();
        scheduleEmailsPojo.setSendTo("a@gmail.com");
        scheduleEmailsPojo.setScheduleId(scheduleId);
        emailsPojoList.add(scheduleEmailsPojo);
        return emailsPojoList;
    }
}
