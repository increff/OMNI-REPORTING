package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ReportQueryPojo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.increff.omni.reporting.helper.ReportTestHelper.getReportQueryPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportQueryApiTest extends AbstractTest {

    @Autowired
    private ReportQueryApi api;

    @Test
    public void testAdd() {
        ReportQueryPojo queryPojo = getReportQueryPojo("select version();", 100001);
        ReportQueryPojo queryPojo2 = getReportQueryPojo("select version2();", 100002);
        api.upsertQuery(queryPojo);
        api.upsertQuery(queryPojo2);
        ReportQueryPojo pojo = api.getByReportId(100001);
        assertNotNull(pojo);
        assertEquals("select version();", pojo.getQuery());
        pojo = api.getByReportId(100002);
        assertNotNull(pojo);
        assertEquals("select version2();", pojo.getQuery());
        pojo.setQuery("select version3();");
        api.upsertQuery(pojo);
        pojo = api.getByReportId(100002);
        assertNotNull(pojo);
        assertEquals("select version3();", pojo.getQuery());
    }
}
