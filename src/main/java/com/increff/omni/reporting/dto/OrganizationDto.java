package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.OrgSchemaApi;
import com.increff.omni.reporting.api.OrganizationApi;
import com.increff.omni.reporting.api.SchemaApi;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.increff.omni.reporting.pojo.SchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrganizationDto extends AbstractDtoApi {

    @Autowired
    private OrganizationApi api;

    @Autowired
    private SchemaApi schemaApi;

    @Autowired
    private OrgSchemaApi orgSchemaApi;

    public OrganizationData add(OrganizationForm form) throws ApiException {
        OrganizationPojo pojo = ConvertUtil.convert(form, OrganizationPojo.class);
        pojo = api.add(pojo);
        return ConvertUtil.convert(pojo, OrganizationData.class);
    }

    public OrganizationData update(OrganizationForm form) throws ApiException {
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

    public OrgSchemaData mapToSchema(Integer orgId, Integer schemaId) throws ApiException {
        //validation
        OrganizationPojo orgPojo = api.getCheck(orgId);
        SchemaPojo schemaPojo = schemaApi.getCheck(schemaId);

        OrgSchemaPojo pojo = createPojo(orgPojo, schemaPojo);
        return getOrgSchemaData(pojo, schemaPojo);
    }

    public List<OrgSchemaData> selectAllOrgSchema(){
        List<OrgSchemaPojo> pojos = orgSchemaApi.selectAll();
        return getOrgSchemaData(pojos);
    }

    private List<OrgSchemaData> getOrgSchemaData(List<OrgSchemaPojo> pojos) {
        List<SchemaPojo> allPojos = schemaApi.selectAll();
        Map<Integer, String> idToNameMap = allPojos.stream()
                .collect(Collectors.toMap(SchemaPojo::getId, SchemaPojo::getName));

        return pojos.stream().map(p -> {
            OrgSchemaData data = ConvertUtil.convert(p, OrgSchemaData.class);
            data.setSchemaName(idToNameMap.get(p.getSchemaId()));
            return data;
        }).collect(Collectors.toList());
    }

    private OrgSchemaData getOrgSchemaData(OrgSchemaPojo pojo, SchemaPojo schemaPojo) {
        OrgSchemaData data = new OrgSchemaData();
        data.setOrgId(pojo.getOrgId());
        data.setSchemaId(pojo.getSchemaId());
        data.setSchemaName(schemaPojo.getName());
        return data;
    }

    private OrgSchemaPojo createPojo(OrganizationPojo orgPojo, SchemaPojo schemaPojo) {
        OrgSchemaPojo pojo = new OrgSchemaPojo();
        pojo.setOrgId(orgPojo.getId());
        pojo.setSchemaId(schemaPojo.getId());
        return orgSchemaApi.map(pojo);
    }

}
