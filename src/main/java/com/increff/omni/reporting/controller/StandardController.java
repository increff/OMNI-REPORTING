package com.increff.omni.reporting.controller;


import com.increff.account.client.AuthClient;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.nextscm.commons.spring.common.ApiException;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping(value = "/standard")
public class StandardController {

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
    private ApplicationProperties properties;
    @Autowired
    private AuthClient authClient;

    @Operation(summary = "Get all available timezones")
    @RequestMapping(value = "/timeZones", method = RequestMethod.GET)
    public List<TimeZoneData> getAllAvailableTimeZones() throws ApiException {
        return reportRequestDto.getAllAvailableTimeZones();
    }

    @Operation(summary = "Select controls for a report")
    @RequestMapping(value = "/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId) throws ApiException {
        return inputControlDto.selectForReport(reportId);
    }

    @Operation(summary = "Get Reports")
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@RequestParam Boolean isDashboard) throws ApiException {
        return reportDto.selectByOrg(isDashboard);
    }

    @Operation(summary = "Get Live Data")
    @RequestMapping(value = "/reports/live", method = RequestMethod.POST)
    public List<Map<String, String>> getLiveData(@RequestBody ReportRequestForm form) throws ApiException, IOException {
        return reportDto.getLiveData(form);
    }

    @Operation(summary = "Get validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @Operation(summary = "Get All Directories")
    @RequestMapping(value = "/directories", method = RequestMethod.GET)
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @Operation(summary = "Request Report")
    @RequestMapping(value = "/request-report", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @Operation(summary = "Get All Request data")
    @RequestMapping(value = "/request-report", method = RequestMethod.GET)
    public List<ReportRequestData> getAllRequests() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @Operation(summary = "Get Result of Request")
    @RequestMapping(value = "/request-report/{requestId}", method = RequestMethod.GET)
    public String getFile(@PathVariable Integer requestId) throws
            ApiException, IOException {
        return reportRequestDto.getReportFile(requestId);
    }

    @Operation(summary = "View CSV of Request")
    @RequestMapping(value = "/request-report/{requestId}/view", method = RequestMethod.GET)
    public List<Map<String, String>> viewFile(@PathVariable Integer requestId) throws ApiException, IOException {
        return reportRequestDto.viewReport(requestId);
    }

    // Scheduling a Report
    @Operation(summary = "Schedule a Report")
    @RequestMapping(value = "/schedules", method = RequestMethod.POST)
    public void scheduleReport(@RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.scheduleReport(form);
    }

    @Operation(summary = "Get Schedules for an organization")
    @RequestMapping(value = "/schedules", method = RequestMethod.GET)
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReports(pageNo, pageSize);
    }

    @Operation(summary = "Get Schedule requests for an organization")
    @RequestMapping(value = "/schedules/requests", method = RequestMethod.GET)
    public List<ReportRequestData> getScheduleReportRequests(@RequestParam Integer pageNo,
                                                         @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduledRequests(pageNo, pageSize);
    }

    // Scheduling a Report
    @Operation(summary = "Edit Schedule of a Report")
    @RequestMapping(value = "/schedules/{id}", method = RequestMethod.PUT)
    public void editScheduleReport(@PathVariable Integer id, @RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.editScheduleReport(id, form);
    }

    @Operation(summary = "Delete Schedule of a Report")
    @RequestMapping(value = "/schedules/{id}", method = RequestMethod.DELETE)
    public void deleteSchedule(@PathVariable Integer id) throws ApiException {
        reportScheduleDto.deleteSchedule(id);
    }

    @Operation(summary = "Enable / Disable Report Schedule")
    @RequestMapping(value = "/schedules/{id}/status", method = RequestMethod.PATCH)
    public void editStatus(@PathVariable Integer id, @RequestParam Boolean isEnabled) throws ApiException {
        reportScheduleDto.updateStatus(id, isEnabled);
    }

    @Operation(summary = "Get Application Version")
    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public String getVersion() {
        return properties.getVersion();
    }

}
