package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getReportScheduleInputParamsPojo;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getReportSchedulePojo;
import static org.junit.Assert.assertEquals;

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
        reportScheduleApi.addScheduleInputParams(Arrays.asList(paramsPojo), schedulePojo);
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(1, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Client ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002253'", scheduleInputParamsPojoList.get(0).getParamValue());
    }

    @Test
    public void testUpdateScheduleParam() {
        // todo
    }
}
