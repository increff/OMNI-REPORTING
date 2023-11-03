package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.data.Charts.BarChartImpl;
import com.increff.omni.reporting.model.data.Charts.ChartInterface;
import com.increff.omni.reporting.model.data.Charts.PieChartImpl;
import com.increff.omni.reporting.model.form.DashboardForm;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j
@Setter
public class DashboardDto extends AbstractDto {
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DashboardApi api;
    @Autowired
    private DashboardChartApi dashboardChartApi;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private DefaultValueApi defaultValueApi;

    public DashboardData addDashboard(DashboardForm form) throws ApiException {
        checkValid(form);
        DashboardPojo dashboardPojo = ConvertUtil.convert(form, DashboardPojo.class);
        dashboardPojo.setOrgId(getOrgId());
        return getDashboard(api.add(dashboardPojo).getId());
    }

    public List<DashboardListData> getDashboardsByOrgId() {
        return getDashboardsByOrgId(getOrgId());
    }
    public List<DashboardListData> getDashboardsByOrgId(Integer orgId) {
        return ConvertUtil.convert(api.getByOrgId(orgId), DashboardListData.class);
    }
