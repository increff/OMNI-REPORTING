package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(value = "/standard")
public class StandardController {

    @Autowired
    private PipelineDto pipelineDto;
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
    public List<ReportData> selectByOrgId(@RequestParam Boolean isChart,
                                          @RequestParam Optional<VisualizationType> visualization) throws ApiException {
        return reportDto.selectByOrg(isChart, visualization.orElse(null));
    }

    @Operation(summary = "Get Report by Alias")
    @GetMapping(value = "/reports/find")
    public ReportData selectByAlias(@RequestParam Boolean isChart, @RequestParam String alias) throws ApiException {
        return reportDto.selectByAlias(isChart, alias);
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
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo,
                                                       @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReports(pageNo, pageSize);
    }

    @Operation(summary = "Get Schedule by ID")
    @GetMapping(value = "/schedules/{id}")
    public ReportScheduleData getScheduleReports(@PathVariable Integer id) throws ApiException {
        return reportScheduleDto.getScheduleReport(id);
    }

    @Operation(summary = "Get Schedule requests for an organization")
    @GetMapping(value = "/schedules/requests")
    public List<ReportRequestData> getScheduleReportRequests(@RequestParam Integer pageNo,
                                                             @RequestParam Integer pageSize) throws ApiException {
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

    @Operation(summary = "Add Pipeline")
    @RequestMapping(value = "/pipelines", method = RequestMethod.POST)
    public PipelineData addPipeline(@RequestBody PipelineForm form) throws ApiException {
        return pipelineDto.add(form);
    }

    @Operation(summary = "Edit Pipeline")
    @RequestMapping(value = "/pipelines/{id}", method = RequestMethod.PUT)
    public PipelineData editPipeline(@RequestBody PipelineForm form, @PathVariable Integer id) throws ApiException {
        return pipelineDto.update(id, form);
    }

    @Operation(summary = "Get Pipelines By User Org")
    @RequestMapping(value = "/pipelines", method = RequestMethod.GET)
    public List<PipelineData> getPipelinesByUserOrg() throws ApiException{
        return pipelineDto.getPipelinesByUserOrg();
    }

    @Operation(summary = "Get Pipeline by ID")
    @RequestMapping(value = "/pipelines/{id}", method = RequestMethod.GET)
    public PipelineData getPipelineById(@PathVariable Integer id) throws ApiException {
        return pipelineDto.getPipelineById(id);
    }

    @Operation(summary = "Test Pipeline")
    @RequestMapping(value = "/pipelines/test", method = RequestMethod.POST)
    public void testPipeline(@RequestBody PipelineForm form) throws ApiException {
        pipelineDto.testConnection(form);
    }

    @Operation(summary = "Add Dashboard")
    @RequestMapping(value = "/dashboards", method = RequestMethod.POST)
    public DashboardData addDashboard(@RequestBody DashboardAddForm form) throws ApiException {
        return dashboardDto.addDashboard(form);
    }

    @Operation(summary = "Delete Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.DELETE)
    public void deleteDashboard(@PathVariable Integer dashboardId) throws ApiException {
        dashboardDto.deleteDashboard(dashboardId);
    }

    @Operation(summary = "Edit Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.PUT)
    public DashboardData editDashboard(@RequestBody DashboardForm form, @PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.updateDashboard(form, dashboardId);
    }

    @Operation(summary = "Add/Edit Charts in Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/charts", method = RequestMethod.PUT)
    public List<DashboardChartData> addChart(@RequestBody List<DashboardChartForm> forms,
                                             @PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.addDashboardChart(forms, dashboardId);
    }

    @Operation(summary = "Get Charts in Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/charts", method = RequestMethod.GET)
    public List<DashboardChartData> getCharts(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.getDashboardCharts(dashboardId);
    }

    @Operation(summary = "Update Defaults in Dashboard. Also deletes all existing defaults for that dashboard")
    @RequestMapping(value = "/dashboards/defaults", method = RequestMethod.PUT)
    public List<DefaultValueData> addDefaults(@RequestBody List<DefaultValueForm> forms) throws ApiException {
        return dashboardDto.upsertDefaultValues(forms);
    }

    @Operation(summary = "Get Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.GET)
    public DashboardData getDashboard(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.getDashboard(dashboardId);
    }

    @Operation(summary = "Get Dashboards For Org")
    @RequestMapping(value = "/dashboards", method = RequestMethod.GET)
    public List<DashboardListData> getDashboards() throws ApiException {
        return dashboardDto.getDashboardsByOrgId();
    }

    // Change rate limiter filter URL when changing endpoint URL
    @Operation(summary = "View Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/view", method = RequestMethod.POST)
    public List<ViewDashboardData> viewDashboard(@PathVariable Integer dashboardId,
                                                 @RequestBody ReportRequestForm form) throws ApiException, IOException {
        return dashboardDto.viewDashboard(form, dashboardId);
    }

    @Operation(summary = "Get Properties")
    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    public ApplicationPropertiesData getProperties() {
        return dashboardDto.getProperties();
    }

}
