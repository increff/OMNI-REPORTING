package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.CustomReportAccessDao;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomReportAccessApi extends AbstractApi {

    @Autowired
    private CustomReportAccessDao dao;

    public List<CustomReportAccessPojo> getByOrgId(Integer orgId){
        return dao.selectMultiple("orgId", orgId);
    }

    public List<CustomReportAccessPojo> getByReportId(Integer reportId){
        return dao.selectMultiple("reportId", reportId);
    }

}
