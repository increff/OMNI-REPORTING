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

@CrossOrigin
@Api
@RestController
@RequestMapping(value = "/integration")
public class IntegrationController {

    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private DashboardDto dashboardDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private IntegrationDto integrationDto;

    @ApiOperation(value = "Integrate New Organization")
    @RequestMapping(value = "/integrate-new-org", method = RequestMethod.POST)
    public OrgMappingsData add(@RequestBody IntegrationOrgForm form) throws ApiException {
        return integrationDto.integrateNewOrg(form);
    }

    @ApiOperation(value = "Edit Existing Organization")
    @RequestMapping(value = "/edit-existing-org", method = RequestMethod.POST)
    public OrgMappingsData editExistingOrg(@RequestBody IntegrationOrgForm form, @RequestParam String oldSchemaVersionName) throws ApiException {
        return integrationDto.editExistingOrg(form, oldSchemaVersionName);
    }

    @ApiOperation(value = "Copy Dashboard to new organizations. This copies charts only! NOT default values!")
    @RequestMapping(value = "/copy-dashboard-new-orgs", method = RequestMethod.POST)
    public void copyDashboardToNewOrgs(@RequestParam List<Integer> orgIds) throws ApiException {
        dashboardDto.copyDashboardToNewOrgs(orgIds);
    }

    @ApiOperation(value = "Get All Schema")
    @RequestMapping(value = "/schema", method = RequestMethod.GET)
    public List<SchemaVersionData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @ApiOperation(value = "Get all org mappings grouped by orgId")
    @RequestMapping(value = "/orgs/mappings/grouped", method = RequestMethod.GET)
    public List<OrgMappingsGroupedData> selectOrgMappingGroupedDetails() {
        return organizationDto.getOrgMappingGroupedDetails();
    }

}
