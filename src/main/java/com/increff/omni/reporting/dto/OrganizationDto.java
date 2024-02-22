package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgMappingsData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.IntegrationOrgConnectionForm;
import com.increff.omni.reporting.model.form.IntegrationOrgSchemaForm;
import com.increff.omni.reporting.model.form.OrgMappingsForm;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class OrganizationDto extends AbstractDto {

    @Autowired
    private OrganizationApi api;

    @Autowired
    private SchemaVersionApi schemaVersionApi;

    @Autowired
    private OrgMappingApi orgMappingApi;

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




//    public OrgSchemaData mapToSchema(Integer id, Integer schemaVersionId) throws ApiException {
//        //validation
//        OrganizationPojo orgPojo = api.getCheck(id);
//        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getCheck(schemaVersionId);
//        orgMappingApi.saveAudit(id.toString(), AuditActions.ORGANIZATION_SCHEMA_VERSION_MAPPING.toString(),
//                "Map Org to Schema Version", "Mapping org : " + orgPojo.getName() + " to schema " +
//                        "version : " + schemaVersionPojo.getName(),
//                getUserName());
//        OrgMappingPojo pojo = createPojo(orgPojo, schemaVersionPojo);
//        return CommonDtoHelper.getOrgSchemaData(pojo, schemaVersionPojo);
//    }

    @Transactional(rollbackFor = ApiException.class)
    public OrgMappingsData addOrgMapping(OrgMappingsForm form) throws ApiException {
        checkValid(form);
        OrgMappingPojo orgMappingPojo = ConvertUtil.convert(form, OrgMappingPojo.class);
        orgMappingPojo = orgMappingApi.add(orgMappingPojo);
        return ConvertUtil.convert(orgMappingPojo, OrgMappingsData.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrgMappingsData editOrgMappings(Integer orgMappingId, OrgMappingsForm form) throws ApiException {
        checkValid(form);
        OrgMappingPojo orgMappingPojo = ConvertUtil.convert(form, OrgMappingPojo.class);
        orgMappingPojo = orgMappingApi.update(orgMappingId, orgMappingPojo);
        return ConvertUtil.convert(orgMappingPojo, OrgMappingsData.class);
    }

    public List<OrgMappingsData> selectOrgMappingDetails(){
        List<OrgMappingPojo> pojos = orgMappingApi.selectAll();
        return ConvertUtil.convert(pojos, OrgMappingsData.class);
    }

//    public OrgSchemaData mapToSchema(IntegrationOrgSchemaForm form) throws ApiException {
//        // todo : change jenkins job apis
//        OrganizationPojo organizationPojo = api.getByName(form.getOrgName());
//        if(Objects.isNull(organizationPojo)) {
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Organization is not available with name : " + form.getOrgName());
//        }
//        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getByName(form.getSchemaVersionName());
//        if(Objects.isNull(schemaVersionPojo)) {
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Schema is not available with name : " + form.getSchemaVersionName());
//        }
//        orgMappingApi.saveAudit(organizationPojo.getId().toString(),
//                AuditActions.ORGANIZATION_SCHEMA_VERSION_MAPPING.toString(),
//                "Map Org to Schema Version", "Mapping org : " + organizationPojo.getName() + " to schema " +
//                        "version : " + schemaVersionPojo.getName(),
//                getUserName());
//        OrgMappingPojo pojo = createPojo(organizationPojo, schemaVersionPojo);
//        return CommonDtoHelper.getOrgSchemaData(pojo, schemaVersionPojo);
//    }

    public List<OrgSchemaData> selectAllOrgSchema(){
        List<OrgMappingPojo> pojos = orgMappingApi.selectAll();
        List<SchemaVersionPojo> allPojos = schemaVersionApi.selectAll();
        return CommonDtoHelper.getOrgSchemaDataList(pojos, allPojos);
    }

    public List<OrgConnectionData> selectAllOrgConnections(){
        List<OrgConnectionPojo> pojos = orgConnectionApi.selectAll();
        List<ConnectionPojo> allPojos = connectionApi.selectAll();
        return CommonDtoHelper.getOrgConnectionDataList(pojos, allPojos);
    }

    // todo : fix map connections to add/edit connections
//    public OrgConnectionData mapToConnection(Integer id, Integer connectionId) throws ApiException {
//        //validation
//        OrganizationPojo orgPojo = api.getCheck(id);
//        ConnectionPojo connectionPojo = connectionApi.getCheck(connectionId);
//        orgConnectionApi.saveAudit(id.toString(), AuditActions.ORGANIZATION_CONNECTION_MAPPING.toString(),
//                "Map Org to Connection", "Mapping org : " + orgPojo.getName() +
//                        " to connection : " + connectionPojo.getName(),
//                getUserName());
//        OrgConnectionPojo pojo = createPojo(orgPojo, connectionPojo);
//        return CommonDtoHelper.getOrgConnectionData(pojo, connectionPojo);
//    }
//
//    public OrgConnectionData mapToConnection(IntegrationOrgConnectionForm form) throws ApiException {
//        OrganizationPojo organizationPojo = api.getByName(form.getOrgName());
//        if(Objects.isNull(organizationPojo)) {
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Organization is not available with name : " + form.getOrgName());
//        }
//        ConnectionPojo connectionPojo = connectionApi.getByName(form.getConnectionName());
//        if(Objects.isNull(connectionPojo)) {
//            throw new ApiException(ApiStatus.BAD_DATA,
//                    "Connection is not available with name : " + form.getConnectionName());
//        }
//        orgConnectionApi.saveAudit(organizationPojo.getId().toString(),
//                AuditActions.ORGANIZATION_CONNECTION_MAPPING.toString(),
//                "Map Org to Connection", "Mapping org : " + organizationPojo.getName() +
//                        " to connection : " + connectionPojo.getName(),
//                getUserName());
//        OrgConnectionPojo pojo = createPojo(organizationPojo, connectionPojo);
//        return CommonDtoHelper.getOrgConnectionData(pojo, connectionPojo);
//    }

}
