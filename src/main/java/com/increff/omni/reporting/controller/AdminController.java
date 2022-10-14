package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @ApiOperation(value = "Add Connection")
    @ApiErrorResponses
    @RequestMapping(value = "/connections", method = RequestMethod.POST)
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @ApiOperation(value = "Test DB Connection")
    @ApiErrorResponses
    @RequestMapping(value = "/connections/{id}", method = RequestMethod.GET)
    public void testConnection(@PathVariable Integer id) throws ApiException {
        connectionDto.testConnection(id);
    }

    @ApiOperation(value = "Update Connection")
    @ApiErrorResponses
    @RequestMapping(value = "/connections/{id}", method = RequestMethod.PUT)
    public ConnectionData update(@PathVariable Integer id, @RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.update(id, form);
    }

    @ApiOperation(value = "Get All Connections")
    @ApiErrorResponses
    @RequestMapping(value = "/connections", method = RequestMethod.GET)
    public List<ConnectionData> selectAll() {
        return connectionDto.selectAll();
    }

    @ApiOperation(value = "Add Input Control")
    @ApiErrorResponses
    @RequestMapping(value = "/controls", method = RequestMethod.POST)
    public InputControlData addInputControl(@RequestBody InputControlForm form) throws ApiException {
        return inputControlDto.add(form);
    }

    @ApiOperation(value = "Edit Input Control")
    @ApiErrorResponses
    @RequestMapping(value = "/controls/{id}", method = RequestMethod.PUT)
    public InputControlData updateInputControl(@PathVariable Integer id, @RequestBody InputControlForm form) throws ApiException {
        return inputControlDto.update(id, form);
    }

    @ApiOperation(value = "Select all global controls")
    @ApiErrorResponses
    @RequestMapping(value = "/controls/global", method = RequestMethod.GET)
    public List<InputControlData> selectAllGlobal() {
        return inputControlDto.selectAllGlobal();
    }

    @ApiOperation(value = "Select controls for a report")
    @ApiErrorResponses
    @RequestMapping(value = "/controls", method = RequestMethod.GET)
    public List<InputControlData> selectByReportId(@RequestParam Integer reportId) {
        return inputControlDto.selectForReport(reportId);
    }

    @ApiOperation(value = "Add Schema")
    @ApiErrorResponses
    @RequestMapping(value = "/schema", method = RequestMethod.POST)
    public SchemaData add(@RequestBody SchemaForm form) throws ApiException {
        return schemaDto.add(form);
    }

    @ApiOperation(value = "Update Schema")
    @ApiErrorResponses
    @RequestMapping(value = "/schema/{schemaId}", method = RequestMethod.PUT)
    public SchemaData updateSchema(@PathVariable Integer schemaId, @RequestBody SchemaForm form) throws ApiException {
        return schemaDto.update(schemaId, form);
    }

    @ApiOperation(value = "Get All Schema")
    @ApiErrorResponses
    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    public List<SchemaData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @ApiOperation(value = "Add Report")
    @ApiErrorResponses
    @RequestMapping(value = "/reports", method = RequestMethod.POST)
    public ReportData add(@RequestBody ReportForm form) throws ApiException {
        return reportDto.add(form);
    }

    @ApiOperation(value = "Edit Report")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.PUT)
    public ReportData edit(@PathVariable Integer reportId, @RequestBody ReportForm form) throws ApiException {
        return reportDto.edit(reportId, form);
    }

    @ApiOperation(value = "Get Report")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
    public ReportData get(@PathVariable Integer reportId) throws ApiException {
        return reportDto.get(reportId);
    }

    @ApiOperation(value = "Get Reports")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/orgs/{orgId}", method = RequestMethod.GET)
    public List<ReportData> selectByOrgId(@PathVariable Integer orgId) throws ApiException {
        return reportDto.selectAll(orgId);
    }

    @ApiOperation(value = "Add/Edit Report Query")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.POST)
    public ReportQueryData addQuery(@PathVariable Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return reportDto.upsertQuery(reportId, form);
    }

    @ApiOperation(value = "Map control to a report")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.POST)
    public void mapReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.mapToControl(reportId, controlId);
    }

    @ApiOperation(value = "Delete report control")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/{controlId}", method = RequestMethod.DELETE)
    public void deleteReportToControl(@PathVariable Integer reportId, @PathVariable Integer controlId) throws ApiException {
        reportDto.deleteReportControl(reportId, controlId);
    }

    @ApiOperation(value = "Add validation group")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.POST)
    public void addValidationGroup(@PathVariable Integer reportId, @RequestBody ValidationGroupForm groupForm) throws ApiException {
        reportDto.addValidationGroup(reportId, groupForm);
    }

    @ApiOperation(value = "Delete validation group")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.DELETE)
    public void deleteValidationGroup(@PathVariable Integer reportId, @RequestParam String groupName) throws ApiException {
        reportDto.deleteValidationGroup(reportId, groupName);
    }

    @ApiOperation(value = "Get validation group")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/controls/validations", method = RequestMethod.GET)
    public List<ValidationGroupData> getValidationGroups(@PathVariable Integer reportId) {
        return reportDto.getValidationGroups(reportId);
    }

    @ApiOperation(value = "Add report expression")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/expressions", method = RequestMethod.POST)
    public void addReportExpression(@RequestBody ReportExpressionForm form) throws ApiException {
        reportDto.addReportExpression(form);
    }

    @ApiOperation(value = "Edit report expression")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/expressions", method = RequestMethod.PUT)
    public void updateReportExpression(@RequestBody ReportExpressionForm form) throws ApiException {
        reportDto.updateReportExpression(form);
    }

    @ApiOperation(value = "Get report expressions")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/{reportId}/expressions", method = RequestMethod.GET)
    public List<ReportExpressionData> getReportExpressions(@PathVariable Integer reportId) {
        return reportDto.getAllExpressionsByReport(reportId);
    }

    @ApiOperation(value = "Delete report expression")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/expressions/{id}", method = RequestMethod.DELETE)
    public void deleteReportExpression(@PathVariable Integer id) throws ApiException {
        reportDto.deleteReportExpression(id);
    }

    @ApiOperation(value = "Add Organization")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs", method = RequestMethod.POST)
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
    }

    @ApiOperation(value = "Update Organization")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs", method = RequestMethod.PUT)
    public OrganizationData update(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.update(form);
    }

    @ApiOperation(value = "Get All Organizations")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() throws ApiException {
        return organizationDto.selectAll();
    }

    @ApiOperation(value = "Map organization to a schema")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs/{orgId}/schema/{schemaId}", method = RequestMethod.POST)
    public OrgSchemaData addSchemaMapping(@PathVariable Integer orgId, @PathVariable Integer schemaId) throws ApiException {
        return organizationDto.mapToSchema(orgId, schemaId);
    }

    @ApiOperation(value = "Map organization to a connection")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs/{orgId}/connections/{connectionId}", method = RequestMethod.POST)
    public OrgConnectionData addConnectionMapping(@PathVariable Integer orgId, @PathVariable Integer connectionId) throws ApiException {
        return organizationDto.mapToConnection(orgId, connectionId);
    }

    @ApiOperation(value = "Get all org schema mapping")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs/schema/", method = RequestMethod.GET)
    public List<OrgSchemaData> selectAllSchemaMapping() {
        return organizationDto.selectAllOrgSchema();
    }

    @ApiOperation(value = "Get all org connection mapping")
    @ApiErrorResponses
    @RequestMapping(value = "/orgs/connections/", method = RequestMethod.GET)
    public List<OrgConnectionData> selectAllConnectionMapping() {
        return organizationDto.selectAllOrgConnections();
    }

    @ApiOperation(value = "Add Directory")
    @ApiErrorResponses
    @RequestMapping(value = "/directories", method = RequestMethod.POST)
    public DirectoryData add(@RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.add(form);
    }

    @ApiOperation(value = "Update Directory")
    @ApiErrorResponses
    @RequestMapping(value = "/directories/{directoryId}", method = RequestMethod.PUT)
    public DirectoryData update(@PathVariable Integer directoryId, @RequestBody DirectoryForm form) throws ApiException {
        return directoryDto.update(directoryId, form);
    }

    @ApiOperation(value = "Get All Directories")
    @ApiErrorResponses
    @RequestMapping(value = "/directories", method = RequestMethod.GET)
    public List<DirectoryData> selectAllDirectories() throws ApiException {
        return directoryDto.getAllDirectories();
    }

    @ApiOperation(value = "Add Custom Report Access")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/custom-access", method = RequestMethod.POST)
    public void addCustomAccess(@RequestBody CustomReportAccessForm form) throws ApiException {
        customReportAccessDto.addCustomReportAccess(form);
    }

    @ApiOperation(value = "Delete Custom Report Access")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/custom-access/{id}", method = RequestMethod.DELETE)
    public void deleteCustomAccess(@PathVariable Integer id) throws ApiException {
        customReportAccessDto.deleteCustomReportAccess(id);
    }

    @ApiOperation(value = "Get all Custom Report Access")
    @ApiErrorResponses
    @RequestMapping(value = "/reports/custom-access", method = RequestMethod.GET)
    public List<CustomReportAccessData> getAllCustomAccess() throws ApiException {
        return customReportAccessDto.getAllData();
    }

}
