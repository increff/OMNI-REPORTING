package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.increff.omni.reporting.helper.OrganizationDtoHelper.*;

@Service
public class OrganizationDto extends AbstractDtoApi {

    @Autowired
    private OrganizationApi api;

    @Autowired
    private SchemaApi schemaApi;

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

    public OrgSchemaData mapToSchema(Integer id, Integer schemaId) throws ApiException {
        //validation
        OrganizationPojo orgPojo = api.getCheck(id);
        SchemaPojo schemaPojo = schemaApi.getCheck(schemaId);

        OrgSchemaPojo pojo = createPojo(orgPojo, schemaPojo);
        return getOrgSchemaData(pojo, schemaPojo);
    }

    public List<OrgSchemaData> selectAllOrgSchema(){
        List<OrgSchemaPojo> pojos = orgSchemaApi.selectAll();
        List<SchemaPojo> allPojos = schemaApi.selectAll();
        return getOrgSchemaDataList(pojos, allPojos);
    }

    public List<OrgConnectionData> selectAllOrgConnections(){
        List<OrgConnectionPojo> pojos = orgConnectionApi.selectAll();
        List<ConnectionPojo> allPojos = connectionApi.selectAll();
        return getOrgConnectionDataList(pojos, allPojos);
    }

    public OrgConnectionData mapToConnection(Integer id, Integer connectionId) throws ApiException {
        //validation
        OrganizationPojo orgPojo = api.getCheck(id);
        ConnectionPojo connectionPojo = connectionApi.getCheck(connectionId);

        OrgConnectionPojo pojo = createPojo(orgPojo, connectionPojo);
        return getOrgConnectionData(pojo, connectionPojo);
    }

    private OrgSchemaPojo createPojo(OrganizationPojo orgPojo, SchemaPojo schemaPojo) {
        OrgSchemaPojo pojo = new OrgSchemaPojo();
        pojo.setOrgId(orgPojo.getId());
        pojo.setSchemaId(schemaPojo.getId());
        return orgSchemaApi.map(pojo);
    }

    private OrgConnectionPojo createPojo(OrganizationPojo orgPojo, ConnectionPojo connectionPojo) {
        OrgConnectionPojo pojo = new OrgConnectionPojo();
        pojo.setOrgId(orgPojo.getId());
        pojo.setConnectionId(connectionPojo.getId());
        return orgConnectionApi.map(pojo);
    }
}
