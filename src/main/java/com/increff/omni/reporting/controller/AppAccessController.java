package com.increff.omni.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.ViewDashboardData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.ConvertUtil;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.List;

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
    private ObjectMapper objectMapper;

    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(ContentCachingRequestWrapper wrappedRequest) throws ApiException {
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        ReportRequestForm form = ConvertUtil.getJavaObjectFromJson(requestBody, ReportRequestForm.class, objectMapper);

        reportRequestDto.requestReport(form);
    }

    // Change rate limiter filter URL when changing endpoint URL
    @ApiOperation(value = "View Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/view", method = RequestMethod.POST)
    public List<ViewDashboardData> viewDashboard(@PathVariable Integer dashboardId, ContentCachingRequestWrapper wrappedRequest) throws ApiException, IOException {
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        ReportRequestForm form = ConvertUtil.getJavaObjectFromJson(requestBody, ReportRequestForm.class, objectMapper);

        return dashboardDto.viewDashboard(form, dashboardId);
    }
}