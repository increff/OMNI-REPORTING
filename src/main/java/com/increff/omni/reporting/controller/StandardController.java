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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

//@CrossOrigin
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

    @ApiOperation(value = "Get all available timezones")
    @RequestMapping(value = "/timeZones", method = RequestMethod.GET)
    public List<TimeZoneData> getAllAvailableTimeZones() throws ApiException {
        return reportRequestDto.getAllAvailableTimeZones();
    }

    @ApiOperation(value = "Select controls for a report")
    @RequestMapping(value = "/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId) throws ApiException {
        return inputControlDto.selectForReport(reportId);
    }

    @ApiOperation(value = "Get Reports")
    @RequestMapping(value = "/reports/orgs/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId) throws ApiException {
        return reportDto.selectAll(orgId);
    }

    @ApiOperation(value = "Get validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @ApiOperation(value = "Get All Directories")
    @RequestMapping(value = "/directories", method = RequestMethod.GET)
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @ApiOperation(value = "Get All Request data")
    @RequestMapping(value = "/request-report", method = RequestMethod.GET)
    public List<ReportRequestData> getAll(@RequestParam Integer limit) throws ApiException {
        return reportRequestDto.getAll(limit);
    }

    @ApiOperation(value = "Get Result of Request")
    @RequestMapping(value = "/request-report/{requestId}",method = RequestMethod.GET)
    public void getFile(@PathVariable Integer requestId, HttpServletResponse response) throws ApiException, IOException {
        File file = reportRequestDto.getReportFile(requestId);
        FileUtil.createFileResponse(file,response);
        FileUtil.delete(file);
    }

}
