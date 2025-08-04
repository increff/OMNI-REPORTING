package com.increff.omni.reporting.api;

import java.util.List;
import java.util.Objects;

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
        for(BenchmarkPojo pojo : pojos){
            BenchmarkPojo existing = getByReportId(pojo.getReportId());
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
}
