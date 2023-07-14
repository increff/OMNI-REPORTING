package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class ReportScheduleApiTest extends AbstractTest {

    @Autowired
    private ReportScheduleApi reportScheduleApi;

    @Test
    public void testAdd() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        List<ReportSchedulePojo> schedulePojoList = reportScheduleApi.selectByOrgIdAndEnabledStatus(100001, true, 1,
                100);
        assertEquals(1, schedulePojoList.size());
        assertEquals("Report 1", schedulePojoList.get(0).getReportName());
        assertEquals(true, schedulePojoList.get(0).getIsEnabled());
        assertEquals(false, schedulePojoList.get(0).getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojoList.get(0).getCron());
    }

    @Test
    public void testAddScheduleInputParams() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        reportScheduleApi.addScheduleInputParams(List.of(paramsPojo), schedulePojo);
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(1, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Client ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002253'", scheduleInputParamsPojoList.get(0).getParamValue());
    }

    @Test
    public void testGetScheduleParamByScheduleId() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        ReportScheduleInputParamsPojo paramsPojo1 = getReportScheduleInputParamsPojo(schedulePojo.getId(), "whId",
                "'1100002254'", "Wh ID");
        reportScheduleApi.addScheduleInputParams(Arrays.asList(paramsPojo, paramsPojo1), schedulePojo);
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(2, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Client ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002253'", scheduleInputParamsPojoList.get(0).getParamValue());
        assertEquals("whId", scheduleInputParamsPojoList.get(1).getParamKey());
        assertEquals("Wh ID", scheduleInputParamsPojoList.get(1).getDisplayValue());
        assertEquals("'1100002254'", scheduleInputParamsPojoList.get(1).getParamValue());
    }

    @Test
    public void testUpdateScheduleParams() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        ReportScheduleInputParamsPojo paramsPojo1 = getReportScheduleInputParamsPojo(schedulePojo.getId(), "whId",
                "'1100002254'", "Wh ID");
        reportScheduleApi.addScheduleInputParams(Arrays.asList(paramsPojo, paramsPojo1), schedulePojo);
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(2, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Client ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002253'", scheduleInputParamsPojoList.get(0).getParamValue());
        assertEquals("whId", scheduleInputParamsPojoList.get(1).getParamKey());
        assertEquals("Wh ID", scheduleInputParamsPojoList.get(1).getDisplayValue());
        assertEquals("'1100002254'", scheduleInputParamsPojoList.get(1).getParamValue());
        ReportScheduleInputParamsPojo paramsPojo2 = getReportScheduleInputParamsPojo(schedulePojo.getId(), "whId",
                "'1100002254'", "Wh ID");
        reportScheduleApi.updateScheduleInputParams(List.of(paramsPojo2), schedulePojo);
        scheduleInputParamsPojoList = reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(1, scheduleInputParamsPojoList.size());
        assertEquals("whId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Wh ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002254'", scheduleInputParamsPojoList.get(0).getParamValue());
    }

    @Test
    public void testGetSchedulesByOrgAndStatus() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", false, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        List<ReportSchedulePojo> schedulePojoList = reportScheduleApi.selectByOrgIdAndEnabledStatus(100001, true, 1,
                100);
        assertEquals(0, schedulePojoList.size());
        schedulePojoList = reportScheduleApi.selectByOrgIdAndEnabledStatus(null, null, 1,
                100);
        assertEquals(1, schedulePojoList.size());
        assertEquals("Report 1", schedulePojoList.get(0).getReportName());
        assertEquals(false, schedulePojoList.get(0).getIsEnabled());
        assertEquals(false, schedulePojoList.get(0).getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojoList.get(0).getCron());
        schedulePojoList = reportScheduleApi.selectByOrgIdAndEnabledStatus(null, null, 2,
                100);
        assertEquals(0, schedulePojoList.size());
    }

    @Test
    public void testGetEligibleSchedules() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        List<ReportSchedulePojo> schedulePojoList = reportScheduleApi.getEligibleSchedules();
        assertEquals(1, schedulePojoList.size());
        assertEquals("Report 1", schedulePojoList.get(0).getReportName());
        assertEquals(true, schedulePojoList.get(0).getIsEnabled());
        assertEquals(false, schedulePojoList.get(0).getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojoList.get(0).getCron());
    }

    @Test
    public void testGetCheck() throws ApiException {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        ReportSchedulePojo schedulePojo1 = reportScheduleApi.getCheck(schedulePojo.getId());
        assertNotNull(schedulePojo1);
        assertEquals("Report 1", schedulePojo1.getReportName());
        assertEquals(true, schedulePojo1.getIsEnabled());
        assertEquals(false, schedulePojo1.getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojo1.getCron());
    }

    @Test
    public void testGetCheckWithException() throws ApiException {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        ApiException exception = assertThrows(ApiException.class, () -> {
            reportScheduleApi.getCheck(schedulePojo.getId() + 1);
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("No report schedule present with id : " + (schedulePojo.getId() + 1), exception.getMessage());
    }

    @Test
    public void testEditSchedule() throws ApiException {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        schedulePojo.setCron("0 */20 * * * ?");
        reportScheduleApi.edit(schedulePojo);
        ReportSchedulePojo schedulePojo1 = reportScheduleApi.getCheck(schedulePojo.getId());
        assertNotNull(schedulePojo1);
        assertEquals("Report 1", schedulePojo1.getReportName());
        assertEquals(true, schedulePojo1.getIsEnabled());
        assertEquals(false, schedulePojo1.getIsDeleted());
        assertEquals("0 */20 * * * ?", schedulePojo1.getCron());
    }

    @Test
    public void testAddEmails() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        List<ReportScheduleEmailsPojo> emailsPojos = getEmailsPojo(schedulePojo.getId());
        reportScheduleApi.addEmails(emailsPojos);
        List<ReportScheduleEmailsPojo> emailsPojoList = reportScheduleApi.getByScheduleId(schedulePojo.getId());
        assertEquals(1, emailsPojoList.size());
        assertEquals("a@gmail.com", emailsPojoList.get(0).getSendTo());
    }

    @Test
    public void testRemoveEmails() {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        List<ReportScheduleEmailsPojo> emailsPojos = getEmailsPojo(schedulePojo.getId());
        reportScheduleApi.addEmails(emailsPojos);
        List<ReportScheduleEmailsPojo> emailsPojoList = reportScheduleApi.getByScheduleId(schedulePojo.getId());
        assertEquals(1, emailsPojoList.size());
        assertEquals("a@gmail.com", emailsPojoList.get(0).getSendTo());
        reportScheduleApi.removeExistingEmails(schedulePojo.getId());
        emailsPojoList = reportScheduleApi.getByScheduleId(schedulePojo.getId());
        assertEquals(0, emailsPojoList.size());
    }

    @Test
    public void testUpdateCount() throws ApiException {
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        reportScheduleApi.add(schedulePojo);
        reportScheduleApi.addScheduleCount(schedulePojo.getId(), 1, 0);
        ReportSchedulePojo schedulePojo1 = reportScheduleApi.getCheck(schedulePojo.getId());
        assertNotNull(schedulePojo1);
        assertEquals("Report 1", schedulePojo1.getReportName());
        assertEquals(true, schedulePojo1.getIsEnabled());
        assertEquals(false, schedulePojo1.getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojo1.getCron());
        assertEquals(11, schedulePojo1.getSuccessCount().intValue());
        assertEquals(0, schedulePojo1.getFailureCount().intValue());
    }

}
