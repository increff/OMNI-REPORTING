package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DirectoryApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.SchemaVersionApi;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.BenchmarkDirection;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.BenchmarkData;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.BenchmarkTestHelper.getBenchmarkForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.jupiter.api.Assertions.*;

public class BenchmarkDtoTest extends AbstractTest {

    @Autowired
    private BenchmarkDto benchmarkDto;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private DirectoryDao directoryDao;

    @Test
    public void testUpsertBenchmarkSuccess() throws ApiException {
        // Setup
        ReportPojo report = setupReportWithBenchmark();

        // Test upsert
        List<BenchmarkData> result = benchmarkDto.upsertBenchmark(getBenchmarkForm(report.getId(), 95.0));

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        BenchmarkData data = result.get(0);
        assertEquals(report.getId(), data.getReportId());
        assertEquals(95.0, data.getValue());
        assertEquals(report.getBenchmarkDirection(), data.getBenchmarkDirection());
        assertEquals(report.getBenchmarkDesc(), data.getBenchmarkDesc());
        assertTrue(data.getLastUpdatedBy().contains("TEST USER"));
    }

    @Test
    public void testUpsertBenchmarkUnsupportedChart() throws ApiException {
        // Setup report with TABLE chart type (doesn't support benchmark)
        ReportPojo report = setupBasicReport();
        report.setChartType(ChartType.TABLE);
        reportApi.add(report);

        // Try to upsert benchmark
        ApiException exception = assertThrows(ApiException.class, () -> {
            benchmarkDto.upsertBenchmark(getBenchmarkForm(report.getId(), 95.0));
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertTrue(exception.getMessage().contains("Chart type does not support benchmark"));
    }

    @Test
    public void testGetBenchmarksForReport() throws ApiException {
        // Setup report with default benchmark
        ReportPojo report = setupReportWithBenchmark();
        
        // Get benchmarks
        List<BenchmarkData> result = benchmarkDto.getBenchmarksForReport(List.of(report.getId()));

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        BenchmarkData data = result.get(0);
        assertEquals(report.getId(), data.getReportId());
        assertEquals(report.getDefaultBenchmark(), data.getValue());
        assertEquals("Default", data.getLastUpdatedBy());
    }

    @Test
    public void testGetBenchmarksForMultipleReports() throws ApiException {
        // Setup two reports
        ReportPojo report1 = setupReportWithBenchmark();
        ReportPojo report2 = setupReportWithBenchmark();
        
        // Get benchmarks
        List<BenchmarkData> result = benchmarkDto.getBenchmarksForReport(List.of(report1.getId(), report2.getId()));

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private ReportPojo setupBasicReport() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Test Reports" + System.currentTimeMillis(), rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1" + System.currentTimeMillis());
        schemaVersionApi.add(schemaVersionPojo);
        
        return getReportPojo("Test Report", ReportType.STANDARD, directoryPojo.getId(), schemaVersionPojo.getId());
    }

    private ReportPojo setupReportWithBenchmark() throws ApiException {
        ReportPojo report = setupBasicReport();
        report.setChartType(ChartType.LINE);
        report.setDefaultBenchmark(90.0);
        report.setBenchmarkDirection(BenchmarkDirection.POSITIVE);
        report.setBenchmarkDesc("Test Benchmark");
        reportApi.add(report);
        return report;
    }
}