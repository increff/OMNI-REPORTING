package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportValidationGroupDao;
import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportValidationGroupApi extends AbstractApi {

    @Autowired
    private ReportValidationGroupDao reportValidationGroupDao;

    public ReportValidationGroupPojo getByNameAndReportId(Integer reportId, String groupName) {
        return reportValidationGroupDao.selectByNameAndId(reportId, groupName);
    }

    public List<ReportValidationGroupPojo> getByReportIdAndReportControlId(Integer reportId, List<Integer> reportControlIds) {
        return reportValidationGroupDao.selectByIdAndControlIdList(reportId, reportControlIds);
    }

    public void addAll(List<ReportValidationGroupPojo> validationGroupPojoList) {
        validationGroupPojoList.forEach(v -> reportValidationGroupDao.persist(v));
    }

    public void deleteByReportIdAndGroupName(Integer reportId, String groupName) throws ApiException {
        ReportValidationGroupPojo pojo = getByNameAndReportId(reportId, groupName);
        checkNotNull(pojo, "Validation group does not exist with group name : " + groupName + " for report id : " + reportId);
        reportValidationGroupDao.remove(pojo);
    }

    public List<ReportValidationGroupPojo> getByReportId(Integer reportId) {
        return reportValidationGroupDao.selectMultiple("reportId", reportId);
    }
}
