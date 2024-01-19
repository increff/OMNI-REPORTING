package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Todo internationalization
@CrossOrigin
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

    // App admin APIs

    @Operation(summary = "Add Connection")
    @PostMapping(value = "/connections")
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @Operation(summary = "Test DB Connection")
    @PostMapping(value = "/connections/test")
    public void testConnection(@RequestBody ConnectionForm form) throws ApiException {
        connectionDto.testConnection(form);
    }

    @Operation(summary = "Update Connection")
    @PutMapping(value = "/connections/{id}")
    public ConnectionData update(@PathVariable Integer id, @RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.update(id, form);
    }

    @Operation(summary = "Get All Connections")
    @GetMapping(value = "/connections")
    public List<ConnectionData> selectAll() {
        return connectionDto.selectAll();
    }

    @Operation(summary = "Add Input Control")
    @PostMapping(value = "/controls")
    public InputControlData addInputControl(@RequestBody InputControlForm form) throws ApiException {
        return inputControlDto.add(form);
    }

    @Operation(summary = "Edit Input Control")
    @PutMapping(value = "/controls/{id}")
    public InputControlData updateInputControl(@PathVariable Integer id, @RequestBody InputControlUpdateForm form) throws ApiException {
        return inputControlDto.update(id, form);
    }

    @Operation(summary = "Get Input Control")
    @GetMapping(value = "/controls/{id}")
    public InputControlData getInputControl(@PathVariable Integer id) throws ApiException {
        return inputControlDto.getById(id);
    }

    @Operation(summary = "Select all global controls")
    @GetMapping(value = "/schemas/{schemaVersionId}/controls/global")
    public List<InputControlData> selectAllGlobal(@PathVariable Integer schemaVersionId) throws ApiException {
        return inputControlDto.selectAllGlobal(schemaVersionId);
    }

    @Operation(summary = "Add Schema")
    @PostMapping(value = "/schema")
    public SchemaVersionData add(@RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.add(form);
    }

    @Operation(summary = "Update Schema")
    @PutMapping(value = "/schema/{schemaVersionId}")
    public SchemaVersionData updateSchema(@PathVariable Integer schemaVersionId, @RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.update(schemaVersionId, form);
    }

    @Operation(summary = "Get All Schema")
    @GetMapping(value = "/schema")
    public List<SchemaVersionData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @Operation(summary = "Add Report")
    @PostMapping(value = "/reports")
    public ReportData add(@RequestBody ReportForm form) throws ApiException {
        return reportDto.add(form);
    }

    @Operation(summary = "Edit Report")
    @PutMapping(value = "/reports/{reportId}")
    public ReportData edit(@PathVariable Integer reportId, @RequestBody ReportForm form) throws ApiException {
        return reportDto.edit(reportId, form);
    }

    @Operation(summary = "Enable / Disable Report")
    @PutMapping(value = "/reports/{reportId}/status")
    public void editStatus(@PathVariable Integer reportId, @RequestParam Boolean isEnabled) throws ApiException {
         reportDto.updateStatus(reportId, isEnabled);
    }

    @Operation(summary = "Get Report")
    @GetMapping(value = "/reports/{reportId}")
    public ReportData get(@PathVariable Integer reportId) throws ApiException {
        return reportDto.get(reportId);
    }

    @Operation(summary = "Get All Report")
    @GetMapping(value = "/reports/schema-versions/{schemaVersionId}")
    public List<ReportData> getAll(@PathVariable Integer schemaVersionId) throws ApiException {
        return reportDto.selectAllBySchemaVersion(schemaVersionId);
    }

    @Operation(summary = "Copy Schema Reports")
    @PostMapping(value = "/copy-reports")
    public void copyReports(@RequestBody CopyReportsForm form) throws ApiException {
        reportDto.copyReports(form);
    }

    @Operation(summary = "Add/Edit Report Query")
    @PostMapping(value = "/reports/{reportId}/query")
    public ReportQueryData addQuery(@PathVariable("reportId") Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return reportDto.upsertQuery(reportId, form);
    }

    @Operation(summary = "Get transformed report query")
    @PostMapping(value = "/reports/query/try")
    public ReportQueryData getTransformedQuery(@RequestBody ReportQueryTestForm form) {
        return reportDto.getTransformedQuery(form);
    }

    @Operation(summary = "Get Report Query")
    @GetMapping(value = "/reports/{reportId}/query")
    public ReportQueryData getQuery(@PathVariable Integer reportId) throws ApiException {
        return reportDto.getQuery(reportId);
    }

    @Operation(summary = "Map control to a report")
    @PostMapping(value = "/reports/{reportId}/controls/{controlId}")
    public void mapReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.mapToControl(reportId, controlId);
    }

    @Operation(summary = "Delete report control")
    @DeleteMapping(value = "/reports/{reportId}/controls/{controlId}")
    public void deleteReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.deleteReportControl(reportId, controlId);
    }

    @Operation(summary = "Add validation group")
    @PostMapping(value = "/reports/{reportId}/controls/validations")
    public void addValidationGroup(@PathVariable Integer reportId, @RequestBody ValidationGroupForm groupForm) throws ApiException {
        reportDto.addValidationGroup(reportId, groupForm);
    }

    @Operation(summary = "Delete validation group")
    @DeleteMapping(value = "/reports/{reportId}/controls/validations")
    public void deleteValidationGroup(@PathVariable Integer reportId, @RequestParam String groupName) throws ApiException {
        reportDto.deleteValidationGroup(reportId, groupName);
    }

    @Operation(summary = "Add Organization")
    @PostMapping(value = "/orgs")
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
    }

    @Operation(summary = "Update Organization")
    @PutMapping(value = "/orgs")
    public OrganizationData update(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.update(form);
    }

    @Operation(summary = "Map organization to a schema")
    @PostMapping(value = "/orgs/{orgId}/schema/{schemaVersionId}")
    public OrgSchemaData addSchemaMapping(@PathVariable Integer orgId, @PathVariable Integer schemaVersionId) throws ApiException {
        return organizationDto.mapToSchema(orgId, schemaVersionId);
    }

    @Operation(summary = "Map organization to a connection")
    @PostMapping(value = "/orgs/{orgId}/connections/{connectionId}")
    public OrgConnectionData addConnectionMapping(@PathVariable Integer orgId, @PathVariable Integer connectionId) throws ApiException {
        return organizationDto.mapToConnection(orgId, connectionId);
    }

    @Operation(summary = "Get all org schema mapping")
    @GetMapping(value = "/orgs/schema/")
    public List<OrgSchemaData> selectAllSchemaMapping() {
        return organizationDto.selectAllOrgSchema();
    }

    @Operation(summary = "Get all org connection mapping")
    @GetMapping(value = "/orgs/connections/")
    public List<OrgConnectionData> selectAllConnectionMapping() {
        return organizationDto.selectAllOrgConnections();
    }

    @Operation(summary = "Add Directory")
    @PostMapping(value = "/directories")
    public DirectoryData add(@RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.add(form);
    }

    @Operation(summary = "Update Directory")
    @PutMapping(value = "/directories/{directoryId}")
    public DirectoryData update(@PathVariable Integer directoryId, @RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.update(directoryId, form);
    }

    @Operation(summary = "Add Custom Report Access")
    @PostMapping(value = "/reports/custom-access")
    public void addCustomAccess(@RequestBody CustomReportAccessForm form) throws ApiException {
        customReportAccessDto.addCustomReportAccess(form);
    }

    @Operation(summary = "Delete Custom Report Access")
    @DeleteMapping(value = "/reports/custom-access/{id}")
    public void deleteCustomAccess(@PathVariable Integer id) {
        customReportAccessDto.deleteCustomReportAccess(id);
    }

    @Operation(summary = "Get all Custom Report Access")
    @GetMapping(value = "/reports/{reportId}/custom-access")
    public List<CustomReportAccessData> getAllCustomAccess(@PathVariable Integer reportId) throws ApiException {
        return customReportAccessDto.getAllDataByReport(reportId);
    }

    @Operation(description = "Change log level")
    @PutMapping(value = "/log")
    public String changeLogLevel(@RequestParam Level level) {
        Configurator.setLevel("com.increff.omni.reporting", level);
        return String.format("Log level changed successfully to %s", level.toString());
    }

    // Report admin APIs

    @Operation(summary = "Get All Organizations")
    @GetMapping(value = "/orgs")
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }

    @Operation(summary = "Request Report")
    @PostMapping(value = "/request-report/orgs/{orgId}")
    public void requestReport(@RequestBody ReportRequestForm form, @PathVariable Integer orgId) throws ApiException {
        reportRequestDto.requestReportForAnyOrg(form, orgId);
    }

    @Operation(summary = "Get Reports")
    @GetMapping(value = "/reports/orgs/{orgId}")
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId, @RequestParam Boolean isDashboard) throws ApiException {
        return reportDto.selectByOrg(orgId, isDashboard);
    }

    @Operation(summary = "Get Live Data For Any Organization")
    @PostMapping(value = "/orgs/{orgId}/reports/live")
    public List<Map<String, String>> requestReport(@PathVariable("orgId") Integer orgId, @RequestBody ReportRequestForm form)
            throws ApiException, IOException {
        return reportDto.getLiveDataForAnyOrganization(form, orgId);
    }

    @Operation(summary = "Select controls for a report for given organization")
    @GetMapping(value = "/orgs/{orgId}/reports/{reportId}/controls")
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId, @PathVariable Integer orgId) throws ApiException {
        return inputControlDto.selectForReport(reportId, orgId);
    }

    @Operation(summary = "Get Schedules for all organizations")
    @GetMapping(value = "/schedules")
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReportsForAllOrgs(pageNo, pageSize);
    }

}
