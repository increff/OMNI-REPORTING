package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ReportInputParamsPojo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.helper.ReportTestHelper.getReportInputParamsPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReportInputParamsApiTest extends AbstractTest {

    @Autowired
    private ReportInputParamsApi api;

    @Test
    public void testAdd() {
        ReportInputParamsPojo pojo1 = getReportInputParamsPojo(1, "itemId", "'100001'");
        ReportInputParamsPojo pojo2 = getReportInputParamsPojo(1, "clientId", null);
        ReportInputParamsPojo pojo3 = getReportInputParamsPojo(2, "clientId", "'10002243'");
        List<ReportInputParamsPojo> reportInputParamsPojoList = Arrays.asList(pojo1, pojo2, pojo3);
        api.add(reportInputParamsPojoList);
        List<ReportInputParamsPojo> reportInputParamsPojos = api.getInputParamsForReportRequest(1);
        assertEquals(2, reportInputParamsPojos.size());
        assertEquals("itemId", reportInputParamsPojos.get(1).getParamKey());
        assertEquals("'100001'", reportInputParamsPojos.get(1).getParamValue());
        assertEquals("clientId", reportInputParamsPojos.get(0).getParamKey());
        assertNull(reportInputParamsPojos.get(0).getParamValue());
    }
}
