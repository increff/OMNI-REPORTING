package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
@Api
@RestController
@RequestMapping(value = "/admin")
public class AdminController {

    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private InputControlDto inputControlDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private DirectoryDto directoryDto;
    @Autowired
    private CustomReportAccessDto customReportAccessDto;
    @Autowired
    private ReportRequestDto reportRequestDto;
    @Autowired
    private ReportScheduleDto reportScheduleDto;
    @Autowired
    private DashboardDto dashboardDto;

    // App admin APIs

    @ApiOperation(value = "Add Connection")
    @RequestMapping(value = "/connections", method = RequestMethod.POST)
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @ApiOperation(value = "Test DB Connection")
    @RequestMapping(value = "/connections/test", method = RequestMethod.POST)
    public void testConnection(@RequestBody ConnectionForm form) throws ApiException {
        connectionDto.testConnection(form);
    }

    @ApiOperation(value = "Update Connection")
    @RequestMapping(value = "/connections/{id}", method = RequestMethod.PUT)
    public ConnectionData update(@PathVariable Integer id, @RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.update(id, form);
    }

    @ApiOperation(value = "Get All Connections")
    @RequestMapping(value = "/connections", method = RequestMethod.GET)
    public List<ConnectionData> selectAll() {
        return connectionDto.selectAll();
    }

    @ApiOperation(value = "Add Input Control")
    @RequestMapping(value = "/controls", method = RequestMethod.POST)
    public InputControlData addInputControl(@RequestBody InputControlForm form) throws ApiException {
        return inputControlDto.add(form);
    }

    @ApiOperation(value = "Edit Input Control")
    @RequestMapping(value = "/controls/{id}", method = RequestMethod.PUT)
    public InputControlData updateInputControl(@PathVariable Integer id, @RequestBody InputControlUpdateForm form) throws ApiException {
        return inputControlDto.update(id, form);
    }

    @ApiOperation(value = "Get Input Control")
    @RequestMapping(value = "/controls/{id}", method = RequestMethod.GET)
    public InputControlData getInputControl(@PathVariable Integer id) throws ApiException {
        return inputControlDto.getById(id);
    }

    @ApiOperation(value = "Select all global controls")
    @RequestMapping(value = "/schemas/{schemaVersionId}/controls/global", method = RequestMethod.GET)
    public List<InputControlData> selectAllGlobal(@PathVariable Integer schemaVersionId) throws ApiException {
        return inputControlDto.selectAllGlobal(schemaVersionId);
    }

    @ApiOperation(value = "Select controls by scope")
    @RequestMapping(value = "/controls", method = RequestMethod.GET)
    public List<InputControlData> selectAllControls(@RequestParam InputControlScope scope) throws ApiException {
        return inputControlDto.selectAll(scope);
    }

    @ApiOperation(value = "Add Schema")
    @RequestMapping(value = "/schema", method = RequestMethod.POST)
    public SchemaVersionData add(@RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.add(form);
    }

    @ApiOperation(value = "Update Schema")
    @RequestMapping(value = "/schema/{schemaVersionId}", method = RequestMethod.PUT)
    public SchemaVersionData updateSchema(@PathVariable Integer schemaVersionId, @RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.update(schemaVersionId, form);
    }

    @ApiOperation(value = "Get All Schema")
    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    public List<SchemaVersionData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @ApiOperation(value = "Add Report")
    @RequestMapping(value = "/reports", method = RequestMethod.POST)
    public ReportData add(@RequestBody ReportForm form) throws ApiException {
        return reportDto.add(form);
    }

    @ApiOperation(value = "Edit Report")
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.PUT)
    public ReportData edit(@PathVariable Integer reportId, @RequestBody ReportForm form) throws ApiException {
        return reportDto.edit(reportId, form);
    }

    @ApiOperation(value = "Enable / Disable Report")
    @RequestMapping(value = "/reports/{reportId}/status", method = RequestMethod.PUT)
    public void editStatus(@PathVariable Integer reportId, @RequestParam Boolean isEnabled) throws ApiException {
         reportDto.updateStatus(reportId, isEnabled);
    }

    @ApiOperation(value = "Get Report")
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
    public ReportData get(@PathVariable Integer reportId) throws ApiException {
        return reportDto.get(reportId);
    }

    @ApiOperation(value = "Get All Report")
    @RequestMapping(value = "/reports/schema-versions/{schemaVersionId}", method = RequestMethod.GET)
    public List<ReportData> getAll(@PathVariable Integer schemaVersionId, @RequestParam Optional<VisualizationType> visualization) throws ApiException {
        return reportDto.selectAllBySchemaVersion(schemaVersionId, visualization.orElse(null));
    }

    @ApiOperation(value = "Copy Schema Reports")
    @RequestMapping(value = "/copy-reports", method = RequestMethod.POST)
    public void copyReports(@RequestBody CopyReportsForm form) throws ApiException {
        reportDto.copyReports(form);
    }

    @ApiOperation(value = "Add/Edit Report Query")
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.POST)
    public ReportQueryData addQuery(@PathVariable Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return reportDto.upsertQuery(reportId, form);
    }

    @ApiOperation(value = "Get transformed report query")
    @RequestMapping(value = "/reports/query/try", method = RequestMethod.POST)
    public ReportQueryData getTransformedQuery(@RequestBody ReportQueryTestForm form) throws ApiException {
        return reportDto.getTransformedQuery(form);
    }

    @ApiOperation(value = "Test Query Live")
    @RequestMapping(value = "/reports/query/try-live", method = RequestMethod.POST)
    public TestQueryLiveData testQueryLive(@RequestBody ReportRequestForm form, @RequestParam Integer orgId) throws ApiException, IOException {
        return reportDto.testQueryLive(form, orgId);
    }

    @ApiOperation(value = "Get Report Query")
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.GET)
    public ReportQueryData getQuery(@PathVariable Integer reportId) throws ApiException {
        return reportDto.getQuery(reportId);
    }

    @ApiOperation(value = "Map control to a report")
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.POST)
    public void mapReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.mapToControl(reportId, controlId);
    }

    @ApiOperation(value = "Delete report control")
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.DELETE)
    public void deleteReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.deleteReportControl(reportId, controlId);
    }

    @ApiOperation(value = "Add validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.POST)
    public void addValidationGroup(@PathVariable Integer reportId, @RequestBody ValidationGroupForm groupForm) throws ApiException {
        reportDto.addValidationGroup(reportId, groupForm);
    }

    @ApiOperation(value = "Delete validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.DELETE)
    public void deleteValidationGroup(@PathVariable Integer reportId, @RequestParam String groupName) throws ApiException {
        reportDto.deleteValidationGroup(reportId, groupName);
    }

    @ApiOperation(value = "Add Organization")
    @RequestMapping(value = "/orgs", method = RequestMethod.POST)
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
    }

    @ApiOperation(value = "Update Organization")
    @RequestMapping(value = "/orgs", method = RequestMethod.PUT)
    public OrganizationData update(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.update(form);
    }

    @ApiOperation(value = "Map organization to a schema and connection")
    @RequestMapping(value = "/orgs/mappings", method = RequestMethod.POST)
    public OrgMappingsData addOrgMappings(@RequestBody OrgMappingsForm form) throws ApiException {
        return organizationDto.addOrgMapping(form);
    }

    @ApiOperation(value = "Edit Org Mapping")
    @RequestMapping(value = "/orgs/mappings/{id}", method = RequestMethod.PUT)
    public OrgMappingsData editOrgMappings(@PathVariable Integer id, @RequestBody OrgMappingsForm form) throws ApiException {
        return organizationDto.editOrgMappings(id, form);
    }

    @ApiOperation(value = "Get all org mappings")
    @RequestMapping(value = "/orgs/mappings", method = RequestMethod.GET)
    public List<OrgMappingsData> selectOrgMappingDetails() {
        return organizationDto.selectOrgMappingDetails();
    }

    @ApiOperation(value = "Get all org mappings grouped by orgId")
    @RequestMapping(value = "/orgs/mappings/grouped", method = RequestMethod.GET)
    public List<OrgMappingsGroupedData> selectOrgMappingGroupedDetails() {
        return organizationDto.selectOrgMappingGroupedDetails();
    }

    @ApiOperation(value = "Get all org schema mapping")
    @RequestMapping(value = "/orgs/schema/", method = RequestMethod.GET)
    public List<OrgSchemaData> selectAllSchemaMapping() {
        return organizationDto.selectAllOrgSchema();
    }

    @ApiOperation(value = "Add Directory")
    @RequestMapping(value = "/directories", method = RequestMethod.POST)
    public DirectoryData add(@RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.add(form);
    }

    @ApiOperation(value = "Update Directory")
    @RequestMapping(value = "/directories/{directoryId}", method = RequestMethod.PUT)
    public DirectoryData update(@PathVariable Integer directoryId, @RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.update(directoryId, form);
    }

    @ApiOperation(value = "Add Custom Report Access")
    @RequestMapping(value = "/reports/custom-access", method = RequestMethod.POST)
    public void addCustomAccess(@RequestBody CustomReportAccessForm form) throws ApiException {
        customReportAccessDto.addCustomReportAccess(form);
    }

    @ApiOperation(value = "Delete Custom Report Access")
    @RequestMapping(value = "/reports/custom-access/{id}", method = RequestMethod.DELETE)
    public void deleteCustomAccess(@PathVariable Integer id) {
        customReportAccessDto.deleteCustomReportAccess(id);
    }

    @ApiOperation(value = "Get all Custom Report Access")
    @RequestMapping(value = "/reports/{reportId}/custom-access", method = RequestMethod.GET)
    public List<CustomReportAccessData> getAllCustomAccess(@PathVariable Integer reportId) throws ApiException {
        return customReportAccessDto.getAllDataByReport(reportId);
    }

    @ApiOperation(value = "Change Log Level")
    @RequestMapping(value = "/log", method = RequestMethod.PUT)
    public void changeLogLevel(@RequestParam Level level) {
        LogManager.getRootLogger().setLevel(level);
    }


    // Report admin APIs

    @ApiOperation(value = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }

    @ApiOperation(value = "Request Report")
    @RequestMapping(value = "/request-report/orgs/{orgId}", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form, @PathVariable Integer orgId) throws ApiException {
        reportRequestDto.requestReportForAnyOrg(form, orgId);
    }

    @ApiOperation(value = "Get Reports")
    @RequestMapping(value = "/reports/orgs/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId, @RequestParam Boolean isChart, @RequestParam Optional<VisualizationType> visualization) throws ApiException {
        return reportDto.selectByOrg(orgId, isChart, visualization.orElse(null));
    }

    @ApiOperation(value = "Select controls for a report for given organization")
    @RequestMapping(value = "/orgs/{orgId}/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId, @PathVariable Integer orgId) throws ApiException {
        return inputControlDto.selectForReport(reportId, orgId);
    }

    @ApiOperation(value = "Get Schedules for all organizations")
    @RequestMapping(value = "/schedules", method = RequestMethod.GET)
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReportsForAllOrgs(pageNo, pageSize);
    }

    @ApiOperation(value = "Copy Dashboard to all organizations. This copies charts only! NOT default values!")
    @RequestMapping(value = "/copy-dashboard-all-orgs", method = RequestMethod.POST)
    public void copyDashboardToAllOrgs(@RequestParam Integer dashboardId, @RequestParam Integer orgId) throws ApiException {
        dashboardDto.copyDashboardToAllOrgs(dashboardId, orgId);
    }

    @ApiOperation(value = "Copy Dashboard to some organizations. This copies charts only! NOT default values!")
    @RequestMapping(value = "/copy-dashboard-some-orgs", method = RequestMethod.POST)
    public void copyDashboardToSomeOrgs(@RequestParam Integer dashboardId, @RequestParam Integer sourceOrgId, @RequestParam List<Integer> destinationOrgIds) throws ApiException {
        dashboardDto.copyDashboardToSomeOrgs(dashboardId, sourceOrgId, destinationOrgIds);
    }
}
