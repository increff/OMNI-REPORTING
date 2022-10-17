package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportExpressionDao;
import com.increff.omni.reporting.pojo.ReportExpressionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportExpressionApi extends AbstractApi {

    @Autowired
    private ReportExpressionDao dao;

    public void addReportExpression(ReportExpressionPojo pojo) throws ApiException {
        ReportExpressionPojo ex = getByNameAndReportId(pojo.getReportId(), pojo.getExpressionName());
        checkNull(ex, "Report expression already exist for report id : " + pojo.getReportId()
            + " and name : " + pojo.getExpressionName());
        dao.persist(pojo);
    }

    public void updateReportExpression(ReportExpressionPojo pojo) throws ApiException {
        ReportExpressionPojo ex = getByNameAndReportId(pojo.getReportId(), pojo.getExpressionName());
        checkNotNull(ex, "Report expression does not exist for report id : " + pojo.getReportId()
                + " and name : " + pojo.getExpressionName());
        ex.setExpression(pojo.getExpression());
        dao.update(ex);
    }

    public void deleteById(Integer id) throws ApiException {
        ReportExpressionPojo pojo = getCheck(id);
        dao.remove(pojo);
    }

    public ReportExpressionPojo getCheck(Integer id) throws ApiException {
        ReportExpressionPojo ex = dao.select(id);
        checkNotNull(ex, "Report expression does not exist for id : " + id);
        return ex;
    }

    public List<ReportExpressionPojo> getAllByReportId(Integer reportId) {
        return dao.selectMultiple("reportId", reportId);
    }

    public ReportExpressionPojo getByNameAndReportId(Integer reportId, String expressionName) {
        return dao.selectByNameAndReportId(reportId, expressionName);
    }
}
