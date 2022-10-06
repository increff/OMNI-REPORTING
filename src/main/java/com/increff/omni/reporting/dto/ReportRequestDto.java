package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.flow.ReportRequestFlow;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.ReportInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportRequestPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.increff.omni.reporting.helper.ReportRequestDtoHelper.getReportRequestPojo;

@Service
public class ReportRequestDto extends AbstractDto {

    @Autowired
    private ReportRequestFlow flow;

    public void requestReport(ReportRequestForm form) throws ApiException {
        checkValid(form);
        ReportRequestPojo pojo = getReportRequestPojo(form, getOrgId(), getUserId());
        List<ReportInputParamsPojo> reportInputParamsPojoList = getReportInputParamsPojoList(form.getParamMap());
        flow.requestReport(pojo, form.getParamMap(), reportInputParamsPojoList);
    }

    private List<ReportInputParamsPojo> getReportInputParamsPojoList(Map<String, String> paramMap) {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
        return reportInputParamsPojoList;
    }

}
