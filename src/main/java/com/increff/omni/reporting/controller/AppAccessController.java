package com.increff.omni.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.ViewDashboardData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.ConvertUtil;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/standard/app-access")
public class AppAccessController {

    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private DashboardDto dashboardDto;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Request Report")
    @PostMapping(value = "/request-report")
    public void requestReport(ContentCachingRequestWrapper wrappedRequest) throws ApiException {
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        ReportRequestForm form = ConvertUtil.getJavaObjectFromJson(requestBody, ReportRequestForm.class, objectMapper);

        reportRequestDto.requestReport(form);
    }

    // Change rate limiter filter URL when changing endpoint URL
    @Operation(summary = "View Dashboard")
    @PostMapping(value = "/dashboards/{dashboardId}/view")
    public List<ViewDashboardData> viewDashboard(@PathVariable Integer dashboardId, ContentCachingRequestWrapper wrappedRequest) throws ApiException, IOException {
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        ReportRequestForm form = ConvertUtil.getJavaObjectFromJson(requestBody, ReportRequestForm.class, objectMapper);

        return dashboardDto.viewDashboard(form, dashboardId);
    }
}