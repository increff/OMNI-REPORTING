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
        api.upsert(Arrays.asList(pojo));
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
        api.upsert(Arrays.asList(pojo));

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

    @Test
    public void testUpsertEmptyList() throws ApiException {
        // Should not throw any exception
        api.upsert(Arrays.asList());
    }

    @Test
    public void testUpsertNullList() throws ApiException {
        // Should not throw any exception
        api.upsert(null);
    }

    @Test
    public void testUpsertMultipleBenchmarks() throws ApiException {
        // Create multiple benchmark pojos
        BenchmarkPojo pojo1 = getBenchmarkPojo(1, orgId, "Test User (test_user)", 95.5);
        BenchmarkPojo pojo2 = getBenchmarkPojo(2, orgId, "Test User (test_user)", 85.0);
        
        // Upsert both
        api.upsert(Arrays.asList(pojo1, pojo2));

        // Verify both were added correctly
        BenchmarkPojo fetched1 = api.getByReportId(1);
        BenchmarkPojo fetched2 = api.getByReportId(2);

        assertNotNull(fetched1);
        assertEquals(pojo1.getReportId(), fetched1.getReportId());
        assertEquals(pojo1.getValue(), fetched1.getValue());

        assertNotNull(fetched2);
        assertEquals(pojo2.getReportId(), fetched2.getReportId());
        assertEquals(pojo2.getValue(), fetched2.getValue());
    }

    @Test
    public void testUpsertMixedNewAndExisting() throws ApiException {
        // First add one benchmark
        BenchmarkPojo existing = getBenchmarkPojo(1, orgId, "Test User (test_user)", 95.5);
        api.upsert(Arrays.asList(existing));

        // Then upsert both an update to existing and a new one
        BenchmarkPojo updatePojo = getBenchmarkPojo(1, orgId, "Another User (another_user)", 85.0);
        BenchmarkPojo newPojo = getBenchmarkPojo(2, orgId, "Test User (test_user)", 90.0);
        
        api.upsert(Arrays.asList(updatePojo, newPojo));

        // Verify both operations worked
        BenchmarkPojo fetched1 = api.getByReportId(1);
        BenchmarkPojo fetched2 = api.getByReportId(2);

        assertNotNull(fetched1);
        assertEquals(updatePojo.getReportId(), fetched1.getReportId());
        assertEquals(updatePojo.getValue(), fetched1.getValue());
        assertEquals(updatePojo.getLastUpdatedBy(), fetched1.getLastUpdatedBy());

        assertNotNull(fetched2);
        assertEquals(newPojo.getReportId(), fetched2.getReportId());
        assertEquals(newPojo.getValue(), fetched2.getValue());
        assertEquals(newPojo.getLastUpdatedBy(), fetched2.getLastUpdatedBy());
    }
}