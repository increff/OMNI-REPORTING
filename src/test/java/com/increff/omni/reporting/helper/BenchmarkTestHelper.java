package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.BenchmarkDirection;
import com.increff.omni.reporting.model.form.BenchmarkForm;
import com.increff.omni.reporting.model.form.DefaultBenchmarkForm;
import com.increff.omni.reporting.pojo.BenchmarkPojo;

import java.util.Collections;

public class BenchmarkTestHelper {

    public static BenchmarkPojo getBenchmarkPojo(Integer reportId, Integer orgId, String lastUpdatedBy, Double value) {
        BenchmarkPojo pojo = new BenchmarkPojo();
        pojo.setReportId(reportId);
        pojo.setOrgId(orgId);
        pojo.setLastUpdatedBy(lastUpdatedBy);
        pojo.setValue(value);
        return pojo;
    }

    public static BenchmarkForm getBenchmarkForm(Integer reportId, Double value) {
        BenchmarkForm form = new BenchmarkForm();
        BenchmarkForm.Benchmark benchmark = new BenchmarkForm.Benchmark();
        benchmark.setReportId(reportId);
        benchmark.setBenchmark(value);
        form.setBenchmarks(Collections.singletonList(benchmark));
        return form;
    }

    public static DefaultBenchmarkForm getDefaultBenchmarkForm(Integer reportId, Double defaultBenchmark, 
            BenchmarkDirection direction, String description) {
        DefaultBenchmarkForm form = new DefaultBenchmarkForm();
        form.setReportId(reportId);
        form.setDefaultBenchmark(defaultBenchmark);
        form.setBenchmarkDirection(direction);
        form.setBenchmarkDesc(description);
        return form;
    }
}