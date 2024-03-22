package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.data.OrgMappingsData;
import com.increff.omni.reporting.model.data.SchemaVersionData;
import com.increff.omni.reporting.model.form.IntegrationOrgForm;
import com.increff.omni.reporting.model.form.OrgMappingsForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Objects;

@Service
public class IntegrationDto extends AbstractDtoApi {

    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private DashboardDto dashboardDto;

    @Transactional
    public OrgMappingsData integrateNewOrg(IntegrationOrgForm form, Boolean createNewConnection) throws ApiException {
        ConnectionData connectionData = null;
        if(createNewConnection) // do not create UNIFY connection multiple times
            connectionData = connectionDto.add(form.getConnectionForm());
        else
            connectionData = getConnectionData(form.getConnectionForm().getName());

        SchemaVersionData svData = getSchemaVersionData(form.getSchemaVersionName());

        // create org
        organizationDto.add(form.getOrganizationForm());

        OrgMappingsForm orgMappingsForm = new OrgMappingsForm();
        orgMappingsForm.setOrgId(form.getOrganizationForm().getId());
        orgMappingsForm.setSchemaVersionId(svData.getId());
        orgMappingsForm.setConnectionId(connectionData.getId());

        // create org mapping
        OrgMappingsData orgMappingsData = organizationDto.addOrgMapping(orgMappingsForm);

        // copy increff dashboards to new org
        dashboardDto.copyDashboardToNewOrgs(Collections.singletonList(form.getOrganizationForm().getId()), false);

        return orgMappingsData;
    }

    @Transactional
    public OrgMappingsData editExistingOrg(IntegrationOrgForm form, String oldSvName) throws ApiException {
        SchemaVersionData oldSvData = getSchemaVersionData(oldSvName);
        OrgMappingsData oldOrgMappingData = getOrgMappingsData(form.getOrganizationForm().getId(), oldSvData.getId());
        SchemaVersionData newSvData = getSchemaVersionData(form.getSchemaVersionName());

        OrgMappingsForm orgMappingsForm = new OrgMappingsForm();
        orgMappingsForm.setOrgId(form.getOrganizationForm().getId());
        orgMappingsForm.setSchemaVersionId(newSvData.getId());
        orgMappingsForm.setConnectionId(oldOrgMappingData.getConnectionId()); // does not edit connection id

        return organizationDto.editOrgMappings(oldOrgMappingData.getId(), orgMappingsForm);
    }


    private SchemaVersionData getSchemaVersionData(String svName) throws ApiException {
        SchemaVersionData svData = schemaDto.selectAll().stream().filter(sv -> sv.getName().equals(svName)).findFirst().orElse(null);
        if(Objects.isNull(svData)){
            throw new ApiException(ApiStatus.BAD_DATA, "Schema version name " + svName + " not found");
        }
        return svData;
    }

    private OrgMappingsData getOrgMappingsData(Integer orgId, Integer svId) throws ApiException {
        OrgMappingsData oldOrgMappingData = organizationDto.getOrgMappingDetails().stream().filter(om ->
                om.getOrgId().equals(orgId) && om.getSchemaVersionId().equals(svId))
                .findFirst().orElse(null);
        if(Objects.isNull(oldOrgMappingData))
            throw new ApiException(ApiStatus.BAD_DATA, "Org mapping not found for orgId " + orgId + " and schema version " + svId);
        return oldOrgMappingData;
    }

    private ConnectionData getConnectionData(String connectionName) throws ApiException {
        ConnectionData connectionData = connectionDto.selectAll().stream().filter(c -> c.getName().equals(connectionName)).findFirst().orElse(null);
        if(Objects.isNull(connectionData)){
            throw new ApiException(ApiStatus.BAD_DATA, "Connection name " + connectionName + " not found");
        }
        return connectionData;
    }

}
