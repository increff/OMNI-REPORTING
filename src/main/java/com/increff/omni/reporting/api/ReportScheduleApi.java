package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportScheduleApi extends AbstractAuditApi {

    @Autowired
    private ReportScheduleDao dao;

    public void add(ReportSchedulePojo pojo) {
        dao.persist(pojo);
    }

    public List<ReportSchedulePojo> selectByOrgId(int orgId) {
        return dao.selectMultiple("orgId", orgId);
    }

    public List<ReportSchedulePojo> getEligibleSchedules() {
        return dao.getEligibleSchedules();
    }
}
