package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.helper.ReportTestHelper.getReportControlsPojo;
import static org.junit.Assert.*;

public class ReportControlsApiTest extends AbstractTest {

    @Autowired
    private ReportControlsApi api;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private ApplicationProperties properties;

    @Test
    public void testAddReportControlPojo() throws ApiException {
        ReportControlsPojo controlsPojo = getReportControlsPojo(100001, 100002);
        api.add(controlsPojo);
        ReportControlsPojo pojo1 = api.getByReportAndControlId(100001, 100002);
        assertNotNull(pojo1);
        assertEquals(100001, pojo1.getReportId().intValue());
        assertEquals(100002, pojo1.getControlId().intValue());
        ReportControlsPojo pojo2 = api.getByReportAndControlId(100001, 100003);
        assertNull(pojo2);
    }

    @Test
    public void testSelectByReportId() throws ApiException {
        ReportControlsPojo controlsPojo = getReportControlsPojo(100001, 100001);
        ReportControlsPojo controlsPojo2 = getReportControlsPojo(100001, 100002);
        ReportControlsPojo controlsPojo3 = getReportControlsPojo(100002, 100001);
        api.add(controlsPojo);
        api.add(controlsPojo2);
        api.add(controlsPojo3);
        List<ReportControlsPojo> reportControlsPojoList = api.getByReportId(100001);
        assertEquals(2, reportControlsPojoList.size());
        assertEquals(100001, reportControlsPojoList.get(0).getControlId().intValue());
        assertEquals(100001, reportControlsPojoList.get(0).getReportId().intValue());
        assertEquals(100002, reportControlsPojoList.get(1).getControlId().intValue());
        assertEquals(100001, reportControlsPojoList.get(1).getReportId().intValue());
    }

    @Test
    public void testGetByIds() throws ApiException {
        ReportControlsPojo controlsPojo = getReportControlsPojo(100001, 100001);
        ReportControlsPojo controlsPojo2 = getReportControlsPojo(100001, 100002);
        ReportControlsPojo controlsPojo3 = getReportControlsPojo(100002, 100001);
        api.add(controlsPojo);
        api.add(controlsPojo2);
        api.add(controlsPojo3);
        List<ReportControlsPojo> reportControlsPojoList =
                api.getByIds(Arrays.asList(controlsPojo2.getId(), controlsPojo3.getId()));
        assertEquals(2, reportControlsPojoList.size());
        assertEquals(100002, reportControlsPojoList.get(0).getControlId().intValue());
        assertEquals(100001, reportControlsPojoList.get(0).getReportId().intValue());
        assertEquals(100001, reportControlsPojoList.get(1).getControlId().intValue());
        assertEquals(100002, reportControlsPojoList.get(1).getReportId().intValue());
    }

    @Test(expected = ApiException.class)
    public void testDelete() throws ApiException {
        ReportControlsPojo controlsPojo = getReportControlsPojo(100001, 100002);
        api.add(controlsPojo);
        api.delete(controlsPojo.getId());
        try {
            api.getCheck(controlsPojo.getId());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report control does not exist for id : " + controlsPojo.getId(), e.getMessage());
            throw e;
        }
    }


}
