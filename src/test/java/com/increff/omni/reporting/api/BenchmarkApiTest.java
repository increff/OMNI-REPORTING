package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.BenchmarkPojo;
import com.increff.commons.springboot.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static com.increff.omni.reporting.helper.BenchmarkTestHelper.getBenchmarkPojo;
import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkApiTest extends AbstractTest {

    @Autowired
    private BenchmarkApi api;

    @Test
    public void testAdd() throws ApiException {
        BenchmarkPojo pojo = getBenchmarkPojo(1, orgId, "Test User (test_user)", 95.5);
        api.add(pojo);
        BenchmarkPojo fetched = api.getByReportId(1);
        assertNotNull(fetched);
        assertEquals(pojo.getReportId(), fetched.getReportId());
        assertEquals(pojo.getOrgId(), fetched.getOrgId());
        assertEquals(pojo.getLastUpdatedBy(), fetched.getLastUpdatedBy());
        assertEquals(pojo.getValue(), fetched.getValue());
    }

    @Test
    public void testUpsertNew() throws ApiException {
        BenchmarkPojo pojo = getBenchmarkPojo(1, orgId, "Test User (test_user)", 95.5);
        api.upsert(Arrays.asList(pojo));
        BenchmarkPojo fetched = api.getByReportId(1);
        assertNotNull(fetched);
        assertEquals(pojo.getReportId(), fetched.getReportId());
        assertEquals(pojo.getValue(), fetched.getValue());
    }

    @Test
    public void testUpsertExisting() throws ApiException {
        // First add
        BenchmarkPojo pojo = getBenchmarkPojo(1, orgId, "Test User (test_user)", 95.5);
        api.add(pojo);

        // Then update via upsert
        BenchmarkPojo updatePojo = getBenchmarkPojo(1, orgId, "Another User (another_user)", 85.0);
        api.upsert(Arrays.asList(updatePojo));

        BenchmarkPojo fetched = api.getByReportId(1);
        assertNotNull(fetched);
        assertEquals(updatePojo.getReportId(), fetched.getReportId());
        assertEquals(updatePojo.getValue(), fetched.getValue());
        assertEquals(updatePojo.getLastUpdatedBy(), fetched.getLastUpdatedBy());
    }

    @Test
    public void testGetByReportIdNotFound() {
        BenchmarkPojo fetched = api.getByReportId(999);
        assertNull(fetched);
    }
}