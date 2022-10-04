package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.helper.FlowApiHelper;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.ReportData;
import com.increff.omni.reporting.model.data.ReportQueryData;
import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.omni.reporting.model.form.ReportQueryForm;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.pojo.ReportQueryPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportDto extends AbstractDtoApi {

    @Autowired
    private ReportFlowApi flowApi;

    public ReportData add(ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo = flowApi.addReport(pojo);
        return ConvertUtil.convert(pojo, ReportData.class);
    }

    public ReportData edit(Integer id, ReportForm form) throws ApiException {
        checkValid(form);
        ReportPojo pojo = ConvertUtil.convert(form, ReportPojo.class);
        pojo.setId(id);
        pojo = flowApi.editReport(pojo);
        return ConvertUtil.convert(pojo, ReportData.class);
    }

    public ReportQueryData upsertQuery(Integer reportId, ReportQueryForm form) throws ApiException {
        checkValid(form);
        ReportQueryPojo pojo = ConvertUtil.convert(form, ReportQueryPojo.class);
        pojo.setReportId(reportId);
        pojo = flowApi.upsertQuery(pojo);
        return ConvertUtil.convert(pojo, ReportQueryData.class);
    }


    public List<ReportData> selectAll(Integer orgId) throws ApiException {
        List<ReportPojo> pojos = flowApi.getAll(orgId);
        return ConvertUtil.convert(pojos, ReportData.class);
    }
    
    public void mapToControl(Integer reportId, Integer controlId, ValidationType validationType) throws ApiException {
        if(reportId == null || controlId == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report id or control id cannot be null");
        ReportControlsPojo pojo = FlowApiHelper.getReportControlPojo(reportId, controlId, validationType);
        flowApi.mapControlToReport(pojo);
    }




}
