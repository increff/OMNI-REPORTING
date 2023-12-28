package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.CustomReportAccessApi;
import com.increff.omni.reporting.api.OrganizationApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.CustomReportAccessData;
import com.increff.omni.reporting.model.form.CustomReportAccessForm;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomReportAccessDto extends AbstractDto {

    @Autowired
    private CustomReportAccessApi api;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private OrganizationApi organizationApi;

    public void addCustomReportAccess(CustomReportAccessForm form) throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(form.getReportId());
        if(reportPojo.getType().equals(ReportType.STANDARD))
            throw new ApiException(ApiStatus.BAD_DATA, "Report type is STANDARD, custom access is not required here.");
        CustomReportAccessPojo pojo = ConvertUtil.convert(form, CustomReportAccessPojo.class);
        api.addCustomReportAccessPojo(pojo);
    }

    public void deleteCustomReportAccess(Integer id) {
        api.deleteById(id);
    }

    public List<CustomReportAccessData> getAllDataByReport(Integer reportId) throws ApiException {
        List<CustomReportAccessData> dataList = new ArrayList<>();
        List<CustomReportAccessPojo> pojoList = api.getAllByReportId(reportId);
        for (CustomReportAccessPojo p : pojoList) {
            ReportPojo reportPojo = reportApi.getCheck(p.getReportId());
            OrganizationPojo organizationPojo = organizationApi.getCheck(p.getOrgId());
            CustomReportAccessData data = ConvertUtil.convert(p, CustomReportAccessData.class);
            data.setReportName(reportPojo.getName());
            data.setOrgName(organizationPojo.getName());
            dataList.add(data);
        }
        return dataList;
    }
}
