package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.commons.springboot.common.ApiException;
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

    @Operation(summary = "Get all available timezones")
    @GetMapping(value = "/timeZones")
    public List<TimeZoneData> getAllAvailableTimeZones() throws ApiException {
        return reportRequestDto.getAllAvailableTimeZones();
    }

    @Operation(summary = "Select controls for a report")
    @GetMapping(value = "/reports/{reportId}/controls")
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId) throws ApiException {
        return inputControlDto.selectForReport(reportId);
    }

    @Operation(summary = "Get Reports")
    @GetMapping(value = "/reports")
    public List<ReportData> selectByOrgId(@RequestParam Boolean isDashboard) throws ApiException {
        return reportDto.selectByOrg(isDashboard);
    }

    @Operation(summary = "Get Report by Alias")
    @GetMapping(value = "/reports/find")
    public ReportData selectByAlias(@RequestParam Boolean isDashboard, @RequestParam String alias) throws ApiException {
        return reportDto.selectByAlias(isDashboard, alias);
    }

    @Operation(summary = "Get Live Data")
    @PostMapping(value = "/reports/live")
    public List<Map<String, String>> getLiveData(@RequestBody ReportRequestForm form) throws ApiException, IOException {
        return reportDto.getLiveData(form);
    }

    @Operation(summary = "Get validation group")
    @GetMapping(value = "/reports/{reportId}/controls/validations")
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @Operation(summary = "Get All Directories")
    @GetMapping(value = "/directories")
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @Operation(summary = "Request Report")
    @PostMapping(value = "/request-report")
    public void requestReport(@RequestBody ReportRequestForm form) throws ApiException {
        reportRequestDto.requestReport(form);
    }

    @Operation(summary = "Get All Request data")
    @GetMapping(value = "/request-report")
    public List<ReportRequestData> getAllRequests() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @Operation(summary = "Get Result of Request")
    @GetMapping(value = "/request-report/{requestId}")
    public String getFile(@PathVariable Integer requestId) throws
            ApiException, IOException {
        return reportRequestDto.getReportFile(requestId);
    }

    @Operation(summary = "View CSV of Request")
    @GetMapping(value = "/request-report/{requestId}/view")
    public List<Map<String, String>> viewFile(@PathVariable Integer requestId) throws ApiException, IOException {
        return reportRequestDto.viewReport(requestId);
    }

    // Scheduling a Report
    @Operation(summary = "Schedule a Report")
    @PostMapping(value = "/schedules")
    public void scheduleReport(@RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.scheduleReport(form);
    }

    @Operation(summary = "Get Schedules for an organization")
    @GetMapping(value = "/schedules")
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReports(pageNo, pageSize);
    }

    @Operation(summary = "Get Schedule by ID")
    @GetMapping(value = "/schedules/{id}")
    public ReportScheduleData getScheduleReports(@PathVariable Integer id) throws ApiException {
        return reportScheduleDto.getScheduleReport(id);
    }

    @Operation(summary = "Get Schedule requests for an organization")
    @GetMapping(value = "/schedules/requests")
    public List<ReportRequestData> getScheduleReportRequests(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduledRequests(pageNo, pageSize);
    }

    // Scheduling a Report
    @Operation(summary = "Edit Schedule of a Report")
    @PutMapping(value = "/schedules/{id}")
    public void editScheduleReport(@PathVariable Integer id, @RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.editScheduleReport(id, form);
    }

    @Operation(summary = "Delete Schedule of a Report")
    @DeleteMapping(value = "/schedules/{id}")
    public void deleteSchedule(@PathVariable Integer id) throws ApiException {
        reportScheduleDto.deleteSchedule(id);
    }

    @Operation(summary = "Enable / Disable Report Schedule")
    @PatchMapping(value = "/schedules/{id}/status")
    public void editStatus(@PathVariable Integer id, @RequestParam Boolean isEnabled) throws ApiException {
        reportScheduleDto.updateStatus(id, isEnabled);
    }

    @Operation(summary = "Get Application Version")
    @GetMapping(value = "/version")
    public String getVersion() {
        return properties.getVersion();
    }

}
