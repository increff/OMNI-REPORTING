package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.dto.DirectoryDto;
import com.increff.omni.reporting.dto.InputControlDto;
import com.increff.omni.reporting.dto.ReportDto;
import com.increff.omni.reporting.dto.ReportRequestDto;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;


//TODO handle timezone
@Api
@RestController
@RequestMapping(value = "/standard")
public class StandardController {

    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private DirectoryDto directoryDto;

    @ApiOperation(value = "Select controls for a report")
    @ApiErrorResponses
    @RequestMapping(value = "/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@RequestParam Integer reportId) {
        return inputControlDto.selectForReport(reportId);
    }

    @ApiOperation(value = "Get Reports")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/orgs/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId) throws ApiException {
        return reportDto.selectAll(orgId);
    }

    @ApiOperation(value = "Get validation group")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @ApiOperation(value = "Get All Directories")
    @ApiErrorResponses
    @RequestMapping(value = "/directories", method = RequestMethod.GET)
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @ApiOperation(value = "Request Report")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @ApiOperation(value = "Get All Request data")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report", method = RequestMethod.GET)
    public List<ReportRequestData> getAll(@RequestParam(required = false) Integer days) throws ApiException {
        return reportRequestDto.getAll(days);
    }

    @ApiOperation(value = "Get Result of Request")
    @ApiErrorResponses
    @RequestMapping(value = "/request-report/{requestId}",method = RequestMethod.GET)
    public void getFile(@PathVariable Integer requestId, HttpServletResponse response) throws ApiException, IOException {
        File file = reportRequestDto.getReportFile(requestId);
        FileUtil.createFileResponse(file,response);
        FileUtil.delete(file);
    }

}
