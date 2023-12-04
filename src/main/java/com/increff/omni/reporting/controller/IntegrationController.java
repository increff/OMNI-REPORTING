package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.ConnectionDto;
import com.increff.omni.reporting.dto.OrganizationDto;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.model.form.IntegrationOrgConnectionForm;
import com.increff.omni.reporting.model.form.IntegrationOrgSchemaForm;
import com.increff.omni.reporting.model.form.OrganizationForm;
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

    @ApiOperation(value = "Map organization to a connection")
    @RequestMapping(value = "/map-connection", method = RequestMethod.POST)
    public OrgConnectionData addConnectionMapping(@RequestBody IntegrationOrgConnectionForm form) throws ApiException {
        return organizationDto.mapToConnection(form);
    }

    @ApiOperation(value = "Map organization to a schema")
    @RequestMapping(value = "/map-schema-version", method = RequestMethod.POST)
    public OrgSchemaData addSchemaMapping(@RequestBody IntegrationOrgSchemaForm form) throws ApiException {
        return organizationDto.mapToSchema(form);
    }

    @ApiOperation(value = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }
}
