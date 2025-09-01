package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.data.BenchmarkData;
import com.increff.omni.reporting.model.form.BenchmarkForm;
import com.increff.omni.reporting.pojo.BenchmarkPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.util.ValidateUtil;
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
        ValidateUtil.validateUpsertBenchmarkForm(form);
        List<ReportPojo> reports = reportApi.getByIds(form.getBenchmarks().stream().map(BenchmarkForm.Benchmark::getReportId).collect(Collectors.toList()));
        if(form.getBenchmarks().size() != reports.size()){
            throw new ApiException(ApiStatus.BAD_DATA, "Some of the reports do not exist");
        }
        for(ReportPojo report : reports){
            if(!report.getChartType().getCAN_BENCHMARK())
                throw  new ApiException(ApiStatus.BAD_DATA, "Chart type does not support benchmark");
        }
        List<BenchmarkPojo> pojoList = getBenchmarkPojoList(form);
        benchmarkApi.upsert(pojoList);
        form.getBenchmarks().forEach(benchmark -> benchmarkApi.saveAudit(benchmark.getReportId().toString(), AuditActions.UPSERT_BENCHMARK.toString(), "upsert benchmark", "value: " + benchmark.getBenchmark().toString(), getUserName()));
        return getBenchmarksForReport(form.getBenchmarks().stream().map(BenchmarkForm.Benchmark::getReportId).collect(Collectors.toList()));
    }

    private static List<BenchmarkPojo> getBenchmarkPojoList(BenchmarkForm form) throws ApiException {
        List<BenchmarkPojo> pojoList = new ArrayList<>();
        for(BenchmarkForm.Benchmark benchmark : form.getBenchmarks()){
            BenchmarkPojo pojo = new BenchmarkPojo();
            pojo.setReportId(benchmark.getReportId());
            pojo.setOrgId(getOrgId());
            pojo.setLastUpdatedBy(getFullName() + " (" + getUserName() + ")");
            pojo.setValue(benchmark.getBenchmark());
            pojoList.add(pojo);
        }
        return pojoList;
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