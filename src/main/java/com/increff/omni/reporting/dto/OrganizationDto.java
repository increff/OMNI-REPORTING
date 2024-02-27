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

    public List<OrgSchemaData> selectAllOrgSchema(){
        List<OrgMappingPojo> pojos = orgMappingApi.selectAll();
        List<SchemaVersionPojo> allPojos = schemaVersionApi.selectAll();
        return CommonDtoHelper.getOrgSchemaDataList(pojos, allPojos);
    }

}
