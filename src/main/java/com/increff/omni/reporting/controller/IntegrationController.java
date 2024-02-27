package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.ConnectionDto;
import com.increff.omni.reporting.dto.DashboardDto;
import com.increff.omni.reporting.dto.OrganizationDto;
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

    @ApiOperation(value = "Add Connection")
    @RequestMapping(value = "/connections", method = RequestMethod.POST)
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @ApiOperation(value = "Add Organization")
    @RequestMapping(value = "/orgs", method = RequestMethod.POST)
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
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

    @ApiOperation(value = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }

    @ApiOperation(value = "Copy Dashboard to new organizations. This copies charts only! NOT default values!")
    @RequestMapping(value = "/copy-dashboard-new-orgs", method = RequestMethod.POST)
    public void copyDashboardToNewOrgs(@RequestParam List<Integer> orgIds) throws ApiException {
        dashboardDto.copyDashboardToNewOrgs(orgIds);
    }

}
