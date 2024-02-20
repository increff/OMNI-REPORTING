package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.data.ValidationGroupData;
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
@RequestMapping(value = "/standard/appAccess")
public class AppAccessController {

    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private ReportScheduleDto reportScheduleDto;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private DirectoryDto directoryDto;
    @Autowired
    private DashboardChartDto dashboardChartDto;
    @Autowired
    private DashboardDto dashboardDto;
    @Autowired
    private ApplicationProperties properties;

    @ApiOperation(value = "Select controls for a report")
    @RequestMapping(value = "/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId) throws ApiException {
        return inputControlDto.selectForReport(reportId);
    }

    @ApiOperation(value = "Get Live Data")
    @RequestMapping(value = "/reports/live", method = RequestMethod.POST)
    public List<Map<String, String>> getLiveData(@RequestBody ReportRequestForm form) throws ApiException, IOException {
        return reportDto.getLiveData(form);
    }


    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @ApiOperation(value = "Get validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    // todo : add alias APIs if reqd

}
