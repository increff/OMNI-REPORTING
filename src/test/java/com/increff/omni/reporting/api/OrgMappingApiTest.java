package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionPojo;
import static com.increff.omni.reporting.helper.OrgMappingTestHelper.getOrgMappingPojo;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OrgMappingApiTest extends AbstractTest {

    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private ConnectionApi connectionApi;

    private Integer schemaVersionId;
    private Integer connectionId;

    @Before
    public void init() throws ApiException {

        organizationApi.add(getOrgPojo(orgId, orgName));
        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.add(getSchemaPojo("9.0.1"));
        this.schemaVersionId = schemaVersionPojo.getId();
        ConnectionPojo connectionPojo = connectionApi.add(getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password"));
        this.connectionId = connectionPojo.getId();
    }

    @Test
    public void testAddOrgMapping() throws ApiException {
        OrgMappingPojo pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        orgMappingApi.add(pojo);
        OrgMappingPojo existing = orgMappingApi.getCheck(pojo.getId());
        assertEquals(pojo.getOrgId(), existing.getOrgId());
        assertEquals(pojo.getSchemaVersionId(), existing.getSchemaVersionId());
        assertEquals(pojo.getConnectionId(), existing.getConnectionId());
    }

    @Test
    public void testAddOrgMappingAnotherSchemaVersionId() throws ApiException {
        assertEquals(orgMappingApi.selectAll().size(), 0);
        OrgMappingPojo pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        orgMappingApi.add(pojo);
        assertEquals(orgMappingApi.selectAll().size(), 1);
        SchemaVersionPojo schemaVersionPojo = schemaVersionApi.add(getSchemaPojo("9.0.2"));
        OrgMappingPojo pojo2 = getOrgMappingPojo(orgId, schemaVersionPojo.getId(), connectionId);
        orgMappingApi.add(pojo2);
        List<OrgMappingPojo> orgMappingPojoList = orgMappingApi.selectAll();
        assertEquals(2, orgMappingPojoList.size());
        assertEquals(pojo.getOrgId(), orgMappingPojoList.get(0).getOrgId());
        assertEquals(pojo.getSchemaVersionId(), orgMappingPojoList.get(0).getSchemaVersionId());
        assertEquals(pojo.getConnectionId(), orgMappingPojoList.get(0).getConnectionId());
        assertEquals(pojo2.getOrgId(), orgMappingPojoList.get(1).getOrgId());
        assertEquals(pojo2.getSchemaVersionId(), orgMappingPojoList.get(1).getSchemaVersionId());
        assertEquals(pojo2.getConnectionId(), orgMappingPojoList.get(1).getConnectionId());
    }

    public void testAddOrgMappingDuplicateSchemaVersionId() throws ApiException {
        OrgMappingPojo pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        orgMappingApi.add(pojo);
        pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        try {
            orgMappingApi.add(pojo);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertTrue(e.getMessage().contains("OrgMapping already exists"));
            throw e;
        }
    }


    @Test
    public void testGetAllOrgs() throws ApiException {
        OrgMappingPojo pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        orgMappingApi.add(pojo);
        List<OrgMappingPojo> orgMappingPojoList = orgMappingApi.selectAll();
        assertEquals(1, orgMappingPojoList.size());
        assertEquals(pojo.getOrgId(), orgMappingPojoList.get(0).getOrgId());
        assertEquals(pojo.getSchemaVersionId(), orgMappingPojoList.get(0).getSchemaVersionId());
        assertEquals(pojo.getConnectionId(), orgMappingPojoList.get(0).getConnectionId());
    }


    @Test
    public void testUpdateOrgMapping() throws ApiException {
        OrgMappingPojo pojo = getOrgMappingPojo(orgId, schemaVersionId, connectionId);
        orgMappingApi.add(pojo);
        pojo.setConnectionId(2);
        orgMappingApi.update(pojo.getId(), pojo);
        OrgMappingPojo existing = orgMappingApi.getCheck(pojo.getId());
        assertEquals(pojo.getOrgId(), existing.getOrgId());
        assertEquals(pojo.getSchemaVersionId(), existing.getSchemaVersionId());
        assertEquals(pojo.getConnectionId(), existing.getConnectionId());
    }
//
    @Test(expected = ApiException.class)
    public void testUpdateOrgWithDuplicateName() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        organizationApi.add(pojo);
        pojo = getOrgPojo(2, "increff2");
        organizationApi.add(pojo);
        pojo = getOrgPojo(1, "increff2");
        try {
            organizationApi.update(pojo);
        } catch (ApiException e) {
            List<OrganizationPojo> organizationPojoList = organizationApi.getAll();
            assertEquals(1, organizationPojoList.get(0).getId().intValue());
            assertEquals("increff", organizationPojoList.get(0).getName());
            assertEquals(2, organizationPojoList.get(1).getId().intValue());
            assertEquals("increff2", organizationPojoList.get(1).getName());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Organization already present with requested name", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testUpdateOrgWithOrgDoesNotExist() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        organizationApi.add(pojo);
        pojo = getOrgPojo(2, "increff2");
        organizationApi.add(pojo);
        pojo = getOrgPojo(3, "increff2");
        try {
            organizationApi.update(pojo);
        } catch (ApiException e) {
            List<OrganizationPojo> organizationPojoList = organizationApi.getAll();
            assertEquals(1, organizationPojoList.get(0).getId().intValue());
            assertEquals("increff", organizationPojoList.get(0).getName());
            assertEquals(2, organizationPojoList.get(1).getId().intValue());
            assertEquals("increff2", organizationPojoList.get(1).getName());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No org present with id : 3", e.getMessage());
            throw e;
        }
    }
}
