package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin
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


    @Operation(summary = "Integrate New Organization")
    @PostMapping(value = "/integrate-new-org")
    public OrgMappingsData add(@RequestBody IntegrationOrgForm form, @RequestParam Boolean createNewConnection) throws ApiException {
        return integrationDto.integrateNewOrg(form, createNewConnection);
    }

    @Operation(summary = "Edit Existing Organization")
    @PostMapping(value = "/edit-existing-org")
    public OrgMappingsData editExistingOrg(@RequestBody IntegrationOrgForm form, @RequestParam String oldSchemaVersionName) throws ApiException {
        return integrationDto.editExistingOrg(form, oldSchemaVersionName);
    }

    @Operation(summary = "Copy Dashboard to new organizations. This copies charts only! NOT default values!")
    @PostMapping(value = "/copy-dashboard-new-orgs")
    public void copyDashboardToNewOrgs(@RequestParam List<Integer> orgIds, @RequestParam(required = false) Boolean copyTestDashboards) throws ApiException {
        dashboardDto.copyDashboardToNewOrgs(orgIds, Objects.isNull(copyTestDashboards) ? false : copyTestDashboards);
    }

    @Operation(summary = "Get All Schema")
    @GetMapping(value = "/schema")
    public List<SchemaVersionData> selectAllSchema() {
        return schemaDto.selectAll();
    }

    @Operation(summary = "Get all org mappings grouped by orgId")
    @GetMapping(value = "/orgs/mappings/grouped")
    public List<OrgMappingsGroupedData> selectOrgMappingGroupedDetails() {
        return organizationDto.getOrgMappingGroupedDetails();
    }

}
