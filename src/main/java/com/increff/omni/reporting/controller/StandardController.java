package com.increff.omni.reporting.controller;


import com.increff.account.client.AuthClient;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@Api
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
    private DashboardChartDto dashboardChartDto;
    @Autowired
    private DashboardDto dashboardDto;
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
    public List<ReportData> selectByOrgId(@RequestParam Boolean isChart, @RequestParam Optional<VisualizationType> visualization) throws ApiException {
        return reportDto.selectByOrg(isChart, visualization.orElse(null));
    }

    @ApiOperation(value = "Get Report by Alias")
    @RequestMapping(value = "/reports/find", method = RequestMethod.GET)
    public ReportData selectByAlias(@RequestParam Boolean isChart, @RequestParam String alias) throws ApiException {
        return reportDto.selectByAlias(isChart, alias);
    }

    @ApiOperation(value = "Get Live Data")
    @RequestMapping(value = "/reports/live", method = RequestMethod.POST)
    public List<Map<String, String>> getLiveData(@RequestBody ReportRequestForm form) throws ApiException, IOException {
        return reportDto.getLiveData(form);
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
    public List<ReportRequestData> getAllRequests() throws ApiException, IOException {
        return reportRequestDto.getAll();
    }

    @ApiOperation(value = "Get Result of Request")
    @RequestMapping(value = "/request-report/{requestId}", method = RequestMethod.GET)
    public String getFile(@PathVariable Integer requestId) throws
            ApiException, IOException {
        return reportRequestDto.getReportFile(requestId);
    }

    @ApiOperation(value = "View CSV of Request")
    @RequestMapping(value = "/request-report/{requestId}/view", method = RequestMethod.GET)
    public List<Map<String, String>> viewFile(@PathVariable Integer requestId) throws ApiException, IOException {
        return reportRequestDto.viewReport(requestId);
    }

    // Scheduling a Report
    @ApiOperation(value = "Schedule a Report")
    @RequestMapping(value = "/schedules", method = RequestMethod.POST)
    public void scheduleReport(@RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.scheduleReport(form);
    }

    @ApiOperation(value = "Get Schedules for an organization")
    @RequestMapping(value = "/schedules", method = RequestMethod.GET)
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReports(pageNo, pageSize);
    }

    @ApiOperation(value = "Get Schedule by ID")
    @RequestMapping(value = "/schedules/{id}", method = RequestMethod.GET)
    public ReportScheduleData getScheduleReports(@PathVariable Integer id) throws ApiException {
        return reportScheduleDto.getScheduleReport(id);
    }

    @ApiOperation(value = "Get Schedule requests for an organization")
    @RequestMapping(value = "/schedules/requests", method = RequestMethod.GET)
    public List<ReportRequestData> getScheduleReportRequests(@RequestParam Integer pageNo,
                                                         @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduledRequests(pageNo, pageSize);
    }

    // Scheduling a Report
    @ApiOperation(value = "Edit Schedule of a Report")
    @RequestMapping(value = "/schedules/{id}", method = RequestMethod.PUT)
    public void editScheduleReport(@PathVariable Integer id, @RequestBody ReportScheduleForm form) throws ApiException {
        reportScheduleDto.editScheduleReport(id, form);
    }

    @ApiOperation(value = "Delete Schedule of a Report")
    @RequestMapping(value = "/schedules/{id}", method = RequestMethod.DELETE)
    public void deleteSchedule(@PathVariable Integer id) throws ApiException {
        reportScheduleDto.deleteSchedule(id);
    }

    @ApiOperation(value = "Enable / Disable Report Schedule")
    @RequestMapping(value = "/schedules/{id}/status", method = RequestMethod.PATCH)
    public void editStatus(@PathVariable Integer id, @RequestParam Boolean isEnabled) throws ApiException {
        reportScheduleDto.updateStatus(id, isEnabled);
    }

    @ApiOperation(value = "Get Application Version")
    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public String getVersion() {
        return properties.getVersion();
    }

    @ApiOperation(value = "Add Dashboard")
    @RequestMapping(value = "/dashboards", method = RequestMethod.POST)
    public DashboardData addDashboard(@RequestBody DashboardAddForm form) throws ApiException {
        return dashboardDto.addDashboard(form);
    }

    @ApiOperation(value = "Delete Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.DELETE)
    public void deleteDashboard(@PathVariable Integer dashboardId) throws ApiException {
        dashboardDto.deleteDashboard(dashboardId);
    }

    @ApiOperation(value = "Edit Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.PUT)
    public DashboardData editDashboard(@RequestBody DashboardForm form, @PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.updateDashboard(form, dashboardId);
    }

    @ApiOperation(value = "Add/Edit Charts in Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/charts", method = RequestMethod.PUT)
    public List<DashboardChartData> addChart(@RequestBody List<DashboardChartForm> forms, @PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.addDashboardChart(forms, dashboardId);
    }

    @ApiOperation(value = "Get Charts in Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/charts", method = RequestMethod.GET)
    public List<DashboardChartData> getCharts(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardChartDto.getDashboardCharts(dashboardId);
    }


    @ApiOperation(value = "Update Defaults in Dashboard. Also deletes all existing defaults for that dashboard")
    @RequestMapping(value = "/dashboards/defaults", method = RequestMethod.PATCH) // TODO: Change to PATCH after UI changes its request type
    public List<DefaultValueData> addDefaults(@RequestBody List<DefaultValueForm> forms) throws ApiException {
        return dashboardDto.upsertDefaultValues(forms);
    }

    @ApiOperation(value = "Get Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}", method = RequestMethod.GET)
    public DashboardData getDashboard(@PathVariable Integer dashboardId) throws ApiException {
        return dashboardDto.getDashboard(dashboardId);
    }

    @ApiOperation(value = "Get Dashboards For Org")
    @RequestMapping(value = "/dashboards", method = RequestMethod.GET)
    public List<DashboardListData> getDashboards() {
        return dashboardDto.getDashboardsByOrgId();
    }

    // Change rate limiter filter URL when changing endpoint URL
    @ApiOperation(value = "View Dashboard")
    @RequestMapping(value = "/dashboards/{dashboardId}/view", method = RequestMethod.POST)
    public List<ViewDashboardData> viewDashboard(@PathVariable Integer dashboardId, @RequestBody ReportRequestForm form) throws ApiException, IOException {
        return dashboardDto.viewDashboard(form, dashboardId);
    }

    @ApiOperation(value = "Get Properties")
    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    public ApplicationPropertiesData getProperties() {
        return dashboardDto.getProperties();
    }

}
