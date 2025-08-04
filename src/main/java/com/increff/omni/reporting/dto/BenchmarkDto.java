package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.model.data.BenchmarkData;
import com.increff.omni.reporting.model.form.BenchmarkForm;
import com.increff.omni.reporting.pojo.BenchmarkPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.api.BenchmarkApi;
import com.increff.omni.reporting.api.ReportApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BenchmarkDto extends AbstractDto {

    @Autowired
    private BenchmarkApi benchmarkApi;
    @Autowired
    private ReportApi reportApi;

    public List<BenchmarkData> upsertBenchmark(BenchmarkForm form) throws ApiException {
        checkValid(form);
        List<ReportPojo> reports = reportApi.getByIds(form.getBenchmarks().stream().map(BenchmarkForm.Benchmark::getReportId).collect(Collectors.toList()));
        for(ReportPojo report : reports){
            if(!report.getChartType().getCAN_BENCHMARK())
                throw  new ApiException(ApiStatus.BAD_DATA, "Chart type does not support benchmark");
        }
        List<BenchmarkPojo> pojoList = new ArrayList<>();
        for(BenchmarkForm.Benchmark benchmark : form.getBenchmarks()){
            BenchmarkPojo pojo = new BenchmarkPojo();
            pojo.setReportId(benchmark.getReportId());
            pojo.setOrgId(getOrgId());
            pojo.setLastUpdatedBy(getFullName() + " (" + getUserName() + ")");
            pojo.setValue(benchmark.getBenchmark());
            pojoList.add(pojo);
        }
        benchmarkApi.upsert(pojoList);
        return getBenchmarksForReport(form.getBenchmarks().stream().map(BenchmarkForm.Benchmark::getReportId).collect(Collectors.toList()));
    }

    public List<BenchmarkData> getBenchmarksForReport(List<Integer> reportIds) throws ApiException {
        List<BenchmarkData> benchmarkDataList = new ArrayList<>();
        List<ReportPojo> reports = reportApi.getByIds(reportIds);
        for(ReportPojo report : reports){
            if(!report.getChartType().getCAN_BENCHMARK())
                continue;
            BenchmarkPojo pojo = benchmarkApi.getByReportId(report.getId());
            BenchmarkData data = getBenchmarkData(report, pojo);
            benchmarkDataList.add(data);
        }
        return benchmarkDataList;
    }

    private static BenchmarkData getBenchmarkData(ReportPojo report, BenchmarkPojo pojo) {
        BenchmarkData data = new BenchmarkData();
        data.setReportId(report.getId());
        data.setBenchmarkDesc(report.getBenchmarkDesc());
        data.setBenchmarkDirection(report.getBenchmarkDirection());
        if(Objects.isNull(pojo)){
            data.setValue(report.getDefaultBenchmark());
            data.setLastUpdatedBy("Default");
        } else {
            data.setValue(pojo.getValue());
            data.setLastUpdatedBy(pojo.getLastUpdatedBy());
            data.setLastUpdatedAt(pojo.getUpdatedAt());
        }
        return data;
    }
} 