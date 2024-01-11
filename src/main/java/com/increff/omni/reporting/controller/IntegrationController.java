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
import com.increff.commons.springboot.common.ApiException;
//import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/integration")
public class IntegrationController {

    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private OrganizationDto organizationDto;

    @Operation(summary = "Add Connection")
    @PostMapping(value = "/connections")
    public ConnectionData add(@RequestBody ConnectionForm form) throws ApiException {
        return connectionDto.add(form);
    }

    @Operation(summary = "Add Organization")
    @PostMapping(value = "/orgs")
    public OrganizationData add(@RequestBody OrganizationForm form) throws ApiException {
        return organizationDto.add(form);
    }

    @Operation(summary = "Map organization to a connection")
    @PostMapping(value = "/map-connection")
    public OrgConnectionData addConnectionMapping(@RequestBody IntegrationOrgConnectionForm form) throws ApiException {
        return organizationDto.mapToConnection(form);
    }

    @Operation(summary = "Map organization to a schema")
    @PostMapping(value = "/map-schema-version")
    public OrgSchemaData addSchemaMapping(@RequestBody IntegrationOrgSchemaForm form) throws ApiException {
        return organizationDto.mapToSchema(form);
    }

    @Operation(summary = "Get All Organizations")
    @RequestMapping(value = "/orgs", method = RequestMethod.GET)
    public List<OrganizationData> selectAllOrgs() {
        return organizationDto.selectAll();
    }
}
