package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
//import org.apache.Log4j2.Level;
//import org.apache.Log4j2.LogManager;
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
    @RequestMapping(value = "/connections", method = RequestMethod.POST)
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @Operation(summary = "Test DB Connection")
    @RequestMapping(value = "/connections/test", method = RequestMethod.POST)
    public void testConnection(@RequestBody ConnectionForm form) throws ApiException {
        connectionDto.testConnection(form);
    }

    @Operation(summary = "Update Connection")
    @RequestMapping(value = "/connections/{id}", method = RequestMethod.PUT)
    public ConnectionData update(@PathVariable Integer id, @RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.update(id, form);
    }

    @Operation(summary = "Get All Connections")
    @RequestMapping(value = "/connections", method = RequestMethod.GET)
    public List<ConnectionData> selectAll() {
        return connectionDto.selectAll();
    }

    @Operation(summary = "Add Input Control")
    @RequestMapping(value = "/controls", method = RequestMethod.POST)
    public InputControlData addInputControl(@RequestBody InputControlForm form) throws ApiException {
        return inputControlDto.add(form);
    }

    @Operation(summary = "Edit Input Control")
    @RequestMapping(value = "/controls/{id}", method = RequestMethod.PUT)
    public InputControlData updateInputControl(@PathVariable Integer id, @RequestBody InputControlUpdateForm form) throws ApiException {
        return inputControlDto.update(id, form);
    }

    @Operation(summary = "Get Input Control")
    @RequestMapping(value = "/controls/{id}", method = RequestMethod.GET)
    public InputControlData getInputControl(@PathVariable Integer id) throws ApiException {
        return inputControlDto.getById(id);
    }

    @Operation(summary = "Select all global controls")
    @RequestMapping(value = "/schemas/{schemaVersionId}/controls/global", method = RequestMethod.GET)
    public List<InputControlData> selectAllGlobal(@PathVariable Integer schemaVersionId) throws ApiException {
        return inputControlDto.selectAllGlobal(schemaVersionId);
    }

    @Operation(summary = "Add Schema")
    @RequestMapping(value = "/schema", method = RequestMethod.POST)
    public SchemaVersionData add(@RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.add(form);
    }

    @Operation(summary = "Update Schema")
    @RequestMapping(value = "/schema/{schemaVersionId}", method = RequestMethod.PUT)
    public SchemaVersionData updateSchema(@PathVariable Integer schemaVersionId, @RequestBody SchemaVersionForm form) throws ApiException {
        return schemaDto.update(schemaVersionId, form);
    }

    @Operation(summary = "Get All Schema")
    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    public List<SchemaVersionData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @Operation(summary = "Add Report")
    @RequestMapping(value = "/reports", method = RequestMethod.POST)
    public ReportData add(@RequestBody ReportForm form) throws ApiException {
        return reportDto.add(form);
    }

    @Operation(summary = "Edit Report")
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.PUT)
    public ReportData edit(@PathVariable Integer reportId, @RequestBody ReportForm form) throws ApiException {
        return reportDto.edit(reportId, form);
    }

    @Operation(summary = "Enable / Disable Report")
    @RequestMapping(value = "/reports/{reportId}/status", method = RequestMethod.PUT)
    public void editStatus(@PathVariable Integer reportId, @RequestParam Boolean isEnabled) throws ApiException {
         reportDto.updateStatus(reportId, isEnabled);
    }

    @Operation(summary = "Get Report")
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
    public ReportData get(@PathVariable Integer reportId) throws ApiException {
        return reportDto.get(reportId);
    }

    @Operation(summary = "Get All Report")
    @RequestMapping(value = "/reports/schema-versions/{schemaVersionId}", method = RequestMethod.GET)
    public List<ReportData> getAll(@PathVariable Integer schemaVersionId) throws ApiException {
        return reportDto.selectAllBySchemaVersion(schemaVersionId);
    }

    @Operation(summary = "Copy Schema Reports")
    @RequestMapping(value = "/copy-reports", method = RequestMethod.POST)
    public void copyReports(@RequestBody CopyReportsForm form) throws ApiException {
        reportDto.copyReports(form);
    }

    @Operation(summary = "Add/Edit Report Query")
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.POST)
    public ReportQueryData addQuery(@PathVariable Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return reportDto.upsertQuery(reportId, form);
    }

    @Operation(summary = "Get transformed report query")
    @RequestMapping(value = "/reports/query/try", method = RequestMethod.POST)
    public ReportQueryData getTransformedQuery(@RequestBody ReportQueryTestForm form) {
        return reportDto.getTransformedQuery(form);
    }

    @Operation(summary = "Get Report Query")
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.GET)
    public ReportQueryData getQuery(@PathVariable Integer reportId) throws ApiException {
        return reportDto.getQuery(reportId);
    }

    @Operation(summary = "Map control to a report")
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.POST)
    public void mapReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.mapToControl(reportId, controlId);
    }

    @Operation(summary = "Delete report control")
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.DELETE)
    public void deleteReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.deleteReportControl(reportId, controlId);
    }

    @Operation(summary = "Add validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.POST)
    public void addValidationGroup(@PathVariable Integer reportId, @RequestBody ValidationGroupForm groupForm) throws ApiException {
        reportDto.addValidationGroup(reportId, groupForm);
    }

    @Operation(summary = "Delete validation group")
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.DELETE)
    public void deleteValidationGroup(@PathVariable Integer reportId, @RequestParam String groupName) throws ApiException {
        reportDto.deleteValidationGroup(reportId, groupName);
    }

    @Operation(summary = "Add Organization")
    @RequestMapping(value = "/orgs", method = RequestMethod.POST)
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
    }

    @Operation(summary = "Update Organization")
    @RequestMapping(value = "/orgs", method = RequestMethod.PUT)
    public OrganizationData update(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.update(form);
    }

    @Operation(summary = "Map organization to a schema")
    @RequestMapping(value = "/orgs/{orgId}/schema/{schemaVersionId}", method = RequestMethod.POST)
    public OrgSchemaData addSchemaMapping(@PathVariable Integer orgId, @PathVariable Integer schemaVersionId) throws ApiException {
        return organizationDto.mapToSchema(orgId, schemaVersionId);
    }

    @Operation(summary = "Map organization to a connection")
    @RequestMapping(value = "/orgs/{orgId}/connections/{connectionId}", method = RequestMethod.POST)
    public OrgConnectionData addConnectionMapping(@PathVariable Integer orgId, @PathVariable Integer connectionId) throws ApiException {
        return organizationDto.mapToConnection(orgId, connectionId);
    }

    @Operation(summary = "Get all org schema mapping")
    @RequestMapping(value = "/orgs/schema/", method = RequestMethod.GET)
    public List<OrgSchemaData> selectAllSchemaMapping() {
        return organizationDto.selectAllOrgSchema();
    }

    @Operation(summary = "Get all org connection mapping")
    @RequestMapping(value = "/orgs/connections/", method = RequestMethod.GET)
    public List<OrgConnectionData> selectAllConnectionMapping() {
        return organizationDto.selectAllOrgConnections();
    }

    @Operation(summary = "Add Directory")
    @RequestMapping(value = "/directories", method = RequestMethod.POST)
    public DirectoryData add(@RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.add(form);
    }

    @Operation(summary = "Update Directory")
    @RequestMapping(value = "/directories/{directoryId}", method = RequestMethod.PUT)
    public DirectoryData update(@PathVariable Integer directoryId, @RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.update(directoryId, form);
    }

    @Operation(summary = "Add Custom Report Access")
    @RequestMapping(value = "/reports/custom-access", method = RequestMethod.POST)
    public void addCustomAccess(@RequestBody CustomReportAccessForm form) throws ApiException {
        customReportAccessDto.addCustomReportAccess(form);
    }

    @Operation(summary = "Delete Custom Report Access")
    @RequestMapping(value = "/reports/custom-access/{id}", method = RequestMethod.DELETE)
    public void deleteCustomAccess(@PathVariable Integer id) {
        customReportAccessDto.deleteCustomReportAccess(id);
    }

    @Operation(summary = "Get all Custom Report Access")
    @RequestMapping(value = "/reports/{reportId}/custom-access", method = RequestMethod.GET)
    public List<CustomReportAccessData> getAllCustomAccess(@PathVariable Integer reportId) throws ApiException {
        return customReportAccessDto.getAllDataByReport(reportId);
    }

//    @Operation(summary = "Change Log Level")
//    @RequestMapping(value = "/log", method = RequestMethod.PUT)
//    public void changeLogLevel(@RequestParam Level level) {
//        LogManager.getRootLogger().setLevel(level);
//    }


    // Report admin APIs

    @Operation(summary = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }

    @Operation(summary = "Request Report")
    @RequestMapping(value = "/request-report/orgs/{orgId}", method = RequestMethod.POST)
    public void requestReport(@RequestBody ReportRequestForm form, @PathVariable Integer orgId) throws ApiException {
        reportRequestDto.requestReportForAnyOrg(form, orgId);
    }

    @Operation(summary = "Get Reports")
    @RequestMapping(value = "/reports/orgs/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId, @RequestParam Boolean isDashboard) throws ApiException {
        return reportDto.selectByOrg(orgId, isDashboard);
    }

    @Operation(summary = "Get Live Data For Any Organization")
    @RequestMapping(value = "/orgs/{orgId}/reports/live", method = RequestMethod.POST)
    public List<Map<String, String>> requestReport(@PathVariable Integer orgId, @RequestBody ReportRequestForm form)
            throws ApiException, IOException {
        return reportDto.getLiveDataForAnyOrganization(form, orgId);
    }

    @Operation(summary = "Select controls for a report for given organization")
    @RequestMapping(value = "/orgs/{orgId}/reports/{reportId}/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@PathVariable Integer reportId, @PathVariable Integer orgId) throws ApiException {
        return inputControlDto.selectForReport(reportId, orgId);
    }

    @Operation(summary = "Get Schedules for all organizations")
    @RequestMapping(value = "/schedules", method = RequestMethod.GET)
    public List<ReportScheduleData> getScheduleReports(@RequestParam Integer pageNo, @RequestParam Integer pageSize) throws ApiException {
        return reportScheduleDto.getScheduleReportsForAllOrgs(pageNo, pageSize);
    }

}
