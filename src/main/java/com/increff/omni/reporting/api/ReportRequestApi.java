package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportRequestDao;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportRequestApi extends AbstractMethodError{

    @Autowired
    private ReportRequestDao dao;

    public ReportRequestPojo add(ReportRequestPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public List<ReportRequestPojo> getPendingByUserId(String userId){
        return dao.getByUserIdAndStatuses(userId,
                Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.IN_PROGRESS));
    }

    public List<ReportRequestPojo> getEligibleRequests(){
        return dao.getEligibleReports(Arrays.asList(ReportRequestStatus.NEW, ReportRequestStatus.STUCK));
    }

    public void markProcessingIfEligible(Integer id){
        ReportRequestPojo pojo = dao.select(id);
        if(pojo.getStatus().equals(ReportRequestStatus.NEW) ||
                (pojo.getStatus().equals(ReportRequestStatus.STUCK))){
            pojo.setStatus(ReportRequestStatus.IN_PROGRESS);
        }
    }

    public void markStuck(){
        List<ReportRequestPojo> stuck = dao.getStuckReports();
        stuck.forEach(s -> {
            s.setStatus(ReportRequestStatus.STUCK);
        });
    }

}
