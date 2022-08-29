package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportControlsDao;
import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportControlsApi {

    @Autowired
    private ReportControlsDao dao;

    public List<ReportControlsPojo> getByReportId(Integer reportId){
        return dao.selectMultiple("reportId", reportId);
    }

}
