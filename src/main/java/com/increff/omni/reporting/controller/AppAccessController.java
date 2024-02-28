package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.data.ValidationGroupData;
import com.increff.omni.reporting.model.data.ViewDashboardData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Api
@RestController
@RequestMapping(value = "/standard/app-access")
public class AppAccessController {

    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private DashboardDto dashboardDto;
    @Autowired
    private ReportDto reportDto;

    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    // Change rate limiter filter URL when changing endpoint URL
    @ApiOperation(value = "View Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/view", method = RequestMethod.POST)
    public List<ViewDashboardData> viewDashboard(@PathVariable Integer dashboardId, @RequestBody ReportRequestForm form) throws ApiException, IOException {
        return dashboardDto.viewDashboard(form, dashboardId);
    }
}