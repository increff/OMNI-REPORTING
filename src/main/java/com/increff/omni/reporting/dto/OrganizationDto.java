package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationDto extends AbstractDto {

    @Autowired
    private OrganizationApi api;

    @Autowired
    private SchemaVersionApi schemaVersionApi;

    @Autowired
    private OrgSchemaApi orgSchemaApi;

    @Autowired
    private OrgConnectionApi orgConnectionApi;

    @Autowired
    private ConnectionApi connectionApi;

    public OrganizationData add(OrganizationForm form) throws ApiException {
        checkValid(form);
        OrganizationPojo pojo = ConvertUtil.convert(form, OrganizationPojo.class);
        pojo = api.add(pojo);
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public OrganizationData update(OrganizationForm form) throws ApiException {
        checkValid(form);
        OrganizationPojo pojo = ConvertUtil.convert(form, OrganizationPojo.class);
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public OrganizationData getById(Integer id) throws ApiException {
        OrganizationPojo pojo = api.getCheck(id);
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public List<OrganizationData> selectAll(){
        List<OrganizationPojo> pojoList = api.getAll();
        return ConvertUtil.convert(pojoList, OrganizationData.class);
    }

    public OrgSchemaData mapToSchema(Integer id, Integer schemaVersionId) throws ApiException {
        //validation
        OrganizationPojo orgPojo = api.getCheck(id);
        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getCheck(schemaVersionId);
        orgSchemaApi.saveAudit(id.toString(), AuditActions.ORGANIZATION_SCHEMA_VERSION_MAPPING.toString(),
                "Map Org to Schema Version", "Mapping org : " + orgPojo.getName() + " to schema version : " + schemaVersionPojo.getName(),
                getUserName());
        OrgSchemaVersionPojo pojo = createPojo(orgPojo, schemaVersionPojo);
        return CommonDtoHelper.getOrgSchemaData(pojo, schemaVersionPojo);
    }

    public List<OrgSchemaData> selectAllOrgSchema(){
        List<OrgSchemaVersionPojo> pojos = orgSchemaApi.selectAll();
        List<SchemaVersionPojo> allPojos = schemaVersionApi.selectAll();
        return CommonDtoHelper.getOrgSchemaDataList(pojos, allPojos);
    }

    public List<OrgConnectionData> selectAllOrgConnections(){
        List<OrgConnectionPojo> pojos = orgConnectionApi.selectAll();
        List<ConnectionPojo> allPojos = connectionApi.selectAll();
        return CommonDtoHelper.getOrgConnectionDataList(pojos, allPojos);
    }

    public OrgConnectionData mapToConnection(Integer id, Integer connectionId) throws ApiException {
        //validation
        OrganizationPojo orgPojo = api.getCheck(id);
        ConnectionPojo connectionPojo = connectionApi.getCheck(connectionId);
        orgConnectionApi.saveAudit(id.toString(), AuditActions.ORGANIZATION_CONNECTION_MAPPING.toString(),
                "Map Org to Connection", "Mapping org : " + orgPojo.getName() + " to connection : " + connectionPojo.getName(),
                getUserName());
        OrgConnectionPojo pojo = createPojo(orgPojo, connectionPojo);
        return CommonDtoHelper.getOrgConnectionData(pojo, connectionPojo);
    }

    private OrgSchemaVersionPojo createPojo(OrganizationPojo orgPojo, SchemaVersionPojo schemaVersionPojo) {
        OrgSchemaVersionPojo pojo = new OrgSchemaVersionPojo();
        pojo.setOrgId(orgPojo.getId());
        pojo.setSchemaVersionId(schemaVersionPojo.getId());
        return orgSchemaApi.map(pojo);
    }

    private OrgConnectionPojo createPojo(OrganizationPojo orgPojo, ConnectionPojo connectionPojo) {
        OrgConnectionPojo pojo = new OrgConnectionPojo();
        pojo.setOrgId(orgPojo.getId());
        pojo.setConnectionId(connectionPojo.getId());
        return orgConnectionApi.map(pojo);
    }
}
