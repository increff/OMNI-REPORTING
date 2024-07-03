package com.increff.omni.reporting.dto;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.common.ConvertUtil;
import com.increff.commons.springboot.common.JsonUtil;
import com.increff.omni.reporting.api.ConnectionApi;
import com.increff.omni.reporting.api.OrgMappingApi;
import com.increff.omni.reporting.api.OrganizationApi;
import com.increff.omni.reporting.api.SchemaVersionApi;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.data.OrgMappingsData;
import com.increff.omni.reporting.model.data.OrgMappingsGroupedData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.OrgMappingsForm;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        api.saveAudit(pojo.getId().toString(), "Organization", AuditActions.ADD_ORGANIZATION.name(), "Added org " + form.getName(), getUserName());
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public OrganizationData update(OrganizationForm form) throws ApiException {
        checkValid(form);
        OrganizationPojo pojo = ConvertUtil.convert(form, OrganizationPojo.class);
        pojo = api.update(pojo);
        api.saveAudit(pojo.getId().toString(), "Organization", AuditActions.EDIT_ORGANIZATION.name(), "Edited org " + form.getName(), getUserName());
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public OrganizationData getById(Integer id) throws ApiException {
        OrganizationPojo pojo = api.get(id);
        return Objects.isNull(pojo) ? null : ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public List<OrganizationData> selectAll(){
        List<OrganizationPojo> pojoList = api.getAll();
        return ConvertUtil.convert(pojoList, OrganizationData.class);
    }


    @Transactional(rollbackFor = ApiException.class)
    public OrgMappingsData addOrgMapping(OrgMappingsForm form) throws ApiException {
        checkValid(form);
        validateOrgMappingForExistingAppName(form);

        OrgMappingPojo orgMappingPojo = ConvertUtil.convert(form, OrgMappingPojo.class);
        orgMappingPojo = orgMappingApi.add(orgMappingPojo);
        api.saveAudit(orgMappingPojo.getId().toString(), "OrgMapping", AuditActions.ADD_ORGANIZATION_MAPPING.name(),
                "Added org mapping " + JsonUtil.serialize(orgMappingPojo), getUserName());
        return ConvertUtil.convert(orgMappingPojo, OrgMappingsData.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    public OrgMappingsData editOrgMappings(Integer orgMappingId, OrgMappingsForm form) throws ApiException {
        checkValid(form);
        validateOrgMappingEditForExistingAppName(orgMappingId, form);

        OrgMappingPojo orgMappingPojo = ConvertUtil.convert(form, OrgMappingPojo.class);
        orgMappingPojo = orgMappingApi.update(orgMappingId, orgMappingPojo);
        api.saveAudit(orgMappingPojo.getId().toString(), "OrgMapping", AuditActions.EDIT_ORGANIZATION_MAPPING.name(),
                "Edited org mapping " + JsonUtil.serialize(orgMappingPojo), getUserName());
        return ConvertUtil.convert(orgMappingPojo, OrgMappingsData.class);
    }

    private void validateOrgMappingForExistingAppName(OrgMappingsForm form) throws ApiException {
        AppName newAppName = schemaVersionApi.getCheck(form.getSchemaVersionId()).getAppName();
        List<Integer> existingSchemaVersionIds = orgMappingApi.getByOrgId(form.getOrgId()).stream().map(OrgMappingPojo::getSchemaVersionId).collect(Collectors.toList());
        List<AppName> existingAppNames = schemaVersionApi.getByIds(existingSchemaVersionIds).stream().map(SchemaVersionPojo::getAppName).collect(Collectors.toList());
        if(existingAppNames.contains(newAppName)){
            throw new ApiException(ApiStatus.BAD_DATA, "App name " + newAppName + " mapping already exists for this organization");
        }
    }

    private void validateOrgMappingEditForExistingAppName(Integer orgMappingId, OrgMappingsForm form) throws ApiException {
        AppName newAppName = schemaVersionApi.getCheck(form.getSchemaVersionId()).getAppName();
        AppName oldAppName = schemaVersionApi.getCheck(orgMappingApi.getCheck(orgMappingId).getSchemaVersionId()).getAppName();
        if(newAppName != oldAppName){ // do not allow to change app name when editing as this may lead to 2 schema versions mapping with same app name for same org
            throw new ApiException(ApiStatus.BAD_DATA, "Schema Versions can only be edited for same app name. New app name: " + newAppName + " Old app name: " + oldAppName);
        }
    }


    public List<OrgMappingsData> getOrgMappingDetails(){
        List<OrgMappingPojo> pojos = orgMappingApi.selectAll();
        return ConvertUtil.convert(pojos, OrgMappingsData.class);
    }

    public List<OrgMappingsGroupedData> getOrgMappingGroupedDetails(){
        List<OrgMappingsData> orgMappingsData = getOrgMappingDetails();
        Map<Integer, List<OrgMappingsData>> orgIdToOrgMappingsData = new HashMap<>();
        for(OrgMappingsData data : orgMappingsData){
            if(orgIdToOrgMappingsData.containsKey(data.getOrgId())){
                orgIdToOrgMappingsData.get(data.getOrgId()).add(data);
            }else{
                orgIdToOrgMappingsData.put(data.getOrgId(), new ArrayList<>(Arrays.asList(data)) ); // do not change this to Collections.singletonList as elements are added in this list
            }
        }

        List<OrgMappingsGroupedData> orgMappingsGroupedData = new ArrayList<>();
        // iterate over orgIdToOrgMappingsData
        for(Map.Entry<Integer, List<OrgMappingsData>> entry : orgIdToOrgMappingsData.entrySet()){
            OrgMappingsGroupedData groupedData = new OrgMappingsGroupedData();
            groupedData.setOrgId(entry.getKey());
            groupedData.setOrgMappingsData(entry.getValue());
            orgMappingsGroupedData.add(groupedData);
        }

        // add null for organizations which do not have any mappings
        List<OrganizationData> organizationData = selectAll();
        for(OrganizationData data : organizationData){
            OrgMappingsGroupedData groupedData = new OrgMappingsGroupedData();
            if(!orgIdToOrgMappingsData.containsKey(data.getId())) {
                groupedData.setOrgId(data.getId());
                OrgMappingsData emptyData = new OrgMappingsData();
                emptyData.setOrgId(data.getId());
                groupedData.setOrgMappingsData(Collections.singletonList(emptyData));
                orgMappingsGroupedData.add(groupedData);
            }
        }
        return orgMappingsGroupedData;
    }

    public List<OrgSchemaData> getAllOrgSchema(){
        List<OrgMappingPojo> pojos = orgMappingApi.selectAll();
        List<SchemaVersionPojo> allPojos = schemaVersionApi.selectAll();
        return CommonDtoHelper.getOrgSchemaDataList(pojos, allPojos);
    }

}
