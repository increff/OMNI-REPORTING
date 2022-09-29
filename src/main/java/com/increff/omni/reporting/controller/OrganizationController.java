package com.increff.omni.reporting.controller;

import com.increff.omni.reporting.dto.OrganizationDto;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.ApiErrorResponses;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api
@RestController
@RequestMapping(value = "/org")
public class OrganizationController {

    @Autowired
    private OrganizationDto dto;

    @ApiOperation(value = "Add Organization")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.POST)
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return dto.add(form);
    }

    @ApiOperation(value = "Update Organization")
    @ApiErrorResponses
    @RequestMapping(method = RequestMethod.PUT)
    public OrganizationData update(@RequestBody OrganizationForm form) throws ApiException {
        return dto.update(form);
    }

    @ApiOperation(value = "Get All Organizations")
    @ApiErrorResponses
    @RequestMapping(value = "/selectAll", method = RequestMethod.GET)
    public List<OrganizationData> selectAll() throws ApiException {
        return dto.selectAll();
    }

    @ApiOperation(value = "Map organization to a schema")
    @ApiErrorResponses
    @RequestMapping(value = "/{orgId}/schema/", method = RequestMethod.POST)
    public OrgSchemaData addSchemaMapping(@PathVariable Integer orgId, @RequestBody Integer schemaId) throws ApiException {
        return dto.mapToSchema(orgId, schemaId);
    }

    @ApiOperation(value = "Map organization to a connection")
    @ApiErrorResponses
    @RequestMapping(value = "/{orgId}/connection/", method = RequestMethod.POST)
    public OrgConnectionData addConnectionMapping(@PathVariable Integer orgId, @RequestBody Integer connectionId) throws ApiException {
        return dto.mapToConnection(orgId, connectionId);
    }

    @ApiOperation(value = "Get all org schema mapping")
    @ApiErrorResponses
    @RequestMapping(value = "/schemaMappings/", method = RequestMethod.GET)
    public List<OrgSchemaData> selectAllSchemaMapping(){
        return dto.selectAllOrgSchema();
    }

    @ApiOperation(value = "Get all org connection mapping")
    @ApiErrorResponses
    @RequestMapping(value = "/connectionMappings/", method = RequestMethod.GET)
    public List<OrgConnectionData> selectAllConnectionMapping(){
        return dto.selectAllOrgConnections();
    }

}
