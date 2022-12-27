package com.increff.omni.reporting.controller;


import com.increff.account.client.AuthClient;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.DirectoryDto;
import com.increff.omni.reporting.dto.InputControlDto;
import com.increff.omni.reporting.dto.ReportDto;
import com.increff.omni.reporting.dto.ReportRequestDto;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin
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
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private AuthClient authClient;

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
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId() throws ApiException {
        return reportDto.selectByOrg();
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
    public List<ReportRequestData> getAll() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @ApiOperation(value = "Get Result of Request")
    @RequestMapping(value = "/request-report/{requestId}", method = RequestMethod.GET)
    public void getFile(@PathVariable Integer requestId, HttpServletResponse response) throws
            ApiException, IOException {
        File file = reportRequestDto.getReportFile(requestId);
        FileUtil.createFileResponse(file, response);
        FileUtil.delete(file);
    }

    @ApiOperation(value = "View CSV of Request")
    @RequestMapping(value = "/request-report/{requestId}/view", method = RequestMethod.GET)
    public List<Map<String, String>> viewFile(@PathVariable Integer requestId) throws ApiException, IOException {
        return reportRequestDto.getJsonFromCsv(requestId);
    }

    @ApiOperation(value = "Get Application Version")
    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public String getVersion() {
        return properties.getVersion();
    }

}
