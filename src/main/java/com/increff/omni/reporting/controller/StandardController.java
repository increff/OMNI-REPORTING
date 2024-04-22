package com.increff.omni.reporting.controller;


import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
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

    @Operation(summary = "Get All Request data")
    @GetMapping(value = "/request-report")
    public List<ReportRequestData> getAllRequests() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @Operation(summary = "Get Result of Request")
    @GetMapping(value = "/request-report/{requestId}")
    public String getFile(@PathVariable Integer requestId) throws
            ApiException, IOException {
        return "\"" + reportRequestDto.getReportFile(requestId) + "\"";
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

    @Operation(summary = "Add Pipeline")
    @PostMapping(value = "/pipelines")
    public PipelineData addPipeline(@RequestBody PipelineForm form) throws ApiException {
        return pipelineDto.add(form);
    }

    @Operation(summary = "Edit Pipeline")
    @PutMapping(value = "/pipelines/{id}")
    public PipelineData editPipeline(@RequestBody PipelineForm form, @PathVariable Integer id) throws ApiException {
        return pipelineDto.update(id, form);
    }

    @Operation(summary = "Get Pipelines By User Org")
    @GetMapping(value = "/pipelines")
    public List<PipelineData> getPipelinesByUserOrg() throws ApiException{
        return pipelineDto.getPipelinesByUserOrg();
    }

    @Operation(summary = "Get Pipeline by ID")
    @GetMapping(value = "/pipelines/{id}")
    public PipelineData getPipelineById(@PathVariable Integer id) throws ApiException {
        return pipelineDto.getPipelineById(id);
    }

    @Operation(summary = "Test Pipeline")
    @PostMapping(value = "/pipelines/test")
    public void testPipeline(@RequestBody PipelineForm form) throws ApiException {
        pipelineDto.testConnection(form);
    }

    @Operation(summary = "Get Application Version")
    @GetMapping(value = "/version")
    public String getVersion() {
        return "\"" + properties.getVersion() + "\"";
    }

    @Operation(summary = "Add Dashboard")
    @PostMapping(value = "/dashboards")
    public DashboardData addDashboard(@RequestBody DashboardAddForm form) throws ApiException {
        return dashboardDto.addDashboard(form);
    }

    @Operation(summary = "Delete Dashboard")
    @DeleteMapping(value = "/dashboards/{dashboardId}")
    public void deleteDashboard(@PathVariable Integer dashboardId) throws ApiException {
        dashboardDto.deleteDashboard(dashboardId);
    }

    @Operation(summary = "Edit Dashboard")
    @PutMapping(value = "/dashboards/{dashboardId}")
    public DashboardData editDashboard(@RequestBody DashboardForm form, @PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.updateDashboard(form, dashboardId);
    }

    @Operation(summary = "Add/Edit Charts in Dashboard")
    @PutMapping(value = "/dashboards/{dashboardId}/charts")
    public List<DashboardChartData> addChart(@RequestBody List<DashboardChartForm> forms, @PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.addDashboardChart(forms, dashboardId);
    }

    @Operation(summary = "Get Charts in Dashboard")
    @GetMapping(value = "/dashboards/{dashboardId}/charts")
    public List<DashboardChartData> getCharts(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.getDashboardCharts(dashboardId);
    }

    @Operation(summary = "Update Defaults in Dashboard. Also deletes all existing defaults for that dashboard")
    @PutMapping(value = "/dashboards/defaults")
    public List<DefaultValueData> addDefaults(@RequestBody UpsertDefaultValueForm form,
                                              @RequestParam Integer dashboardId) throws ApiException {
        return dashboardDto.upsertDefaultValues(form, dashboardId);
    }

    @Operation(summary = "Get Dashboard")
    @GetMapping(value = "/dashboards/{dashboardId}")
    public DashboardData getDashboard(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.getDashboard(dashboardId);
    }

    @Operation(summary = "List Dashboards For Org")
    @GetMapping(value = "/dashboards")
    public List<DashboardListData> getDashboards() throws ApiException {
        return dashboardDto.getDashboardsByOrgId();
    }

    @Operation(summary = "Get Properties")
    @GetMapping(value = "/properties")
    public ApplicationPropertiesData getProperties() {
        return dashboardDto.getProperties();
    }

    @Operation(summary = "Get App Names")
    @GetMapping(value = "/app-names")
    public List<AppName> getAppNames() {
        return Arrays.asList(AppName.values());
    }

}
