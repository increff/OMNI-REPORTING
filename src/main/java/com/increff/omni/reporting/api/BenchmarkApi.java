package com.increff.omni.reporting.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.increff.commons.springboot.common.ApiException;
import com.increff.omni.reporting.dao.BenchmarkDao;
import com.increff.omni.reporting.pojo.BenchmarkPojo;

@Service
@Transactional(rollbackFor = ApiException.class)
public class BenchmarkApi extends AbstractAuditApi {

    @Autowired
    private BenchmarkDao dao;

    public BenchmarkPojo getByReportId(Integer reportId) {  
        return dao.selectByReportId(reportId);
    }

    public void upsert(List<BenchmarkPojo> pojos){
        if(Objects.isNull(pojos) || pojos.isEmpty()){
            return;
        }
        List<BenchmarkPojo> existingBenchmarks = getByReportIds(pojos.stream().map(BenchmarkPojo::getReportId).collect(Collectors.toList()));
        Map<Integer, BenchmarkPojo> reportIdToBenchmarkMap = existingBenchmarks.stream().collect(Collectors.toMap(BenchmarkPojo::getReportId, Function.identity()));
        for(BenchmarkPojo pojo : pojos){
            BenchmarkPojo existing = reportIdToBenchmarkMap.get(pojo.getReportId());
            if(Objects.nonNull(existing)){
                existing.setValue(pojo.getValue());
                existing.setLastUpdatedBy(pojo.getLastUpdatedBy());
                dao.update(existing);
            }
            else{
                dao.add(pojo);
            }
        }
    }

    private List<BenchmarkPojo> getByReportIds(List<Integer> reportIds){
        return dao.selectByReportIds(reportIds);
    }
}
