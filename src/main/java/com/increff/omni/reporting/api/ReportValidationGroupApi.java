package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportValidationGroupDao;
import com.increff.omni.reporting.pojo.ReportValidationGroupPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportValidationGroupApi extends AbstractApi {

    @Autowired
    private ReportValidationGroupDao reportValidationGroupDao;

    public void addAll(List<ReportValidationGroupPojo> validationGroupPojoList) {
        validationGroupPojoList.forEach(v -> reportValidationGroupDao.persist(v));
    }

    public List<ReportValidationGroupPojo> getByNameAndReportId(Integer reportId, String groupName) {
        return reportValidationGroupDao.selectByNameAndId(reportId, groupName);
    }

    public void deleteByReportIdAndGroupName(Integer reportId, String groupName) throws ApiException {
        List<ReportValidationGroupPojo> pojoList = getByNameAndReportId(reportId, groupName);
        if(pojoList.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Validation group does not exist with group name : " + groupName + " for report id : " + reportId);
        pojoList.forEach(p -> reportValidationGroupDao.remove(p));
    }

    public List<ReportValidationGroupPojo> getByReportId(Integer reportId) {
        return reportValidationGroupDao.selectMultiple("reportId", reportId);
    }

    public void deleteByReportIdAndReportControlId(Integer reportId, Integer reportControlId) {
        List<ReportValidationGroupPojo> reportValidationGroupPojoList = reportValidationGroupDao
                .selectByIdAndControlId(reportId, reportControlId);
        reportValidationGroupPojoList.forEach(r -> reportValidationGroupDao.remove(r));
    }
}
