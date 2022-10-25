package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Todo internationalization
// Todo version management for reports
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
    public InputControlData updateInputControl(@PathVariable Integer id) throws ApiException {
        return inputControlDto.getById(id);
    }

    @ApiOperation(value = "Select all global controls")
    @RequestMapping(value = "/controls/global", method = RequestMethod.GET)
    public List<InputControlData> selectAllGlobal() throws ApiException {
        return inputControlDto.selectAllGlobal();
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

    @ApiOperation(value = "Get Report")
    @RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
    public ReportData get(@PathVariable Integer reportId) throws ApiException {
        return reportDto.get(reportId);
    }

    @ApiOperation(value = "Get All Report")
    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public List<ReportData> getAll() throws ApiException {
        return reportDto.selectAll();
    }

    @ApiOperation(value = "Add/Edit Report Query")
    @RequestMapping(value = "/reports/{reportId}/query", method = RequestMethod.POST)
    public ReportQueryData addQuery(@PathVariable Integer reportId, @RequestBody ReportQueryForm form) throws ApiException {
        return reportDto.upsertQuery(reportId, form);
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

    @ApiOperation(value = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() throws ApiException {
        return organizationDto.selectAll();
    }

    @ApiOperation(value = "Map organization to a schema")
    @RequestMapping(value = "/orgs/{orgId}/schema/{schemaVersionId}", method = RequestMethod.POST)
    public OrgSchemaData addSchemaMapping(@PathVariable Integer orgId, @PathVariable Integer schemaVersionId) throws ApiException {
        return organizationDto.mapToSchema(orgId, schemaVersionId);
    }

    @ApiOperation(value = "Map organization to a connection")
    @RequestMapping(value = "/orgs/{orgId}/connections/{connectionId}", method = RequestMethod.POST)
    public OrgConnectionData addConnectionMapping(@PathVariable Integer orgId, @PathVariable Integer connectionId) throws ApiException {
        return organizationDto.mapToConnection(orgId, connectionId);
    }

    @ApiOperation(value = "Get all org schema mapping")
    @RequestMapping(value = "/orgs/schema/", method = RequestMethod.GET)
    public List<OrgSchemaData> selectAllSchemaMapping() {
        return organizationDto.selectAllOrgSchema();
    }

    @ApiOperation(value = "Get all org connection mapping")
    @RequestMapping(value = "/orgs/connections/", method = RequestMethod.GET)
    public List<OrgConnectionData> selectAllConnectionMapping() {
        return organizationDto.selectAllOrgConnections();
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
    public void deleteCustomAccess(@PathVariable Integer id) throws ApiException {
        customReportAccessDto.deleteCustomReportAccess(id);
    }

    @ApiOperation(value = "Get all Custom Report Access")
    @RequestMapping(value = "/reports/custom-access", method = RequestMethod.GET)
    public List<CustomReportAccessData> getAllCustomAccess() throws ApiException {
        return customReportAccessDto.getAllData();
    }

}
