package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgPojo;
import static org.junit.Assert.assertEquals;

public class OrgApiTest extends AbstractTest {

    @Autowired
    private OrganizationApi orgApi;

    @Test
    public void testAddOrg() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = orgApi.getCheck(pojo.getId());
        assertEquals(1, pojo.getId().intValue());
        assertEquals("increff", pojo.getName());
    }

    @Test(expected = ApiException.class)
    public void testAddOrgDuplicateID() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = getOrgPojo(1, "increff2");
        try {
            orgApi.add(pojo);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Organization already present with requested id", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddOrgDuplicateName() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = getOrgPojo(2, "increff");
        try {
            orgApi.add(pojo);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Organization already present with requested name", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testGetAllOrgs() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = getOrgPojo(2, "increff2");
        orgApi.add(pojo);
        List<OrganizationPojo> organizationPojoList = orgApi.getAll();
        assertEquals(2, organizationPojoList.size());
        assertEquals(1, organizationPojoList.get(0).getId().intValue());
        assertEquals(2, organizationPojoList.get(1).getId().intValue());
        assertEquals("increff", organizationPojoList.get(0).getName());
        assertEquals("increff2", organizationPojoList.get(1).getName());
    }

    @Test
    public void testUpdateOrg() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = getOrgPojo(1, "increff2");
        orgApi.update(pojo);
        List<OrganizationPojo> organizationPojoList = orgApi.getAll();
        assertEquals(1, organizationPojoList.get(0).getId().intValue());
        assertEquals("increff2", organizationPojoList.get(0).getName());
    }

    @Test(expected = ApiException.class)
    public void testUpdateOrgWithDuplicateName() throws ApiException {
        OrganizationPojo pojo = getOrgPojo(1, "increff");
        orgApi.add(pojo);
        pojo = getOrgPojo(2, "increff2");
        orgApi.add(pojo);
        pojo = getOrgPojo(1, "increff2");
        try {
            orgApi.update(pojo);
        } catch (ApiException e) {
            List<OrganizationPojo> organizationPojoList = orgApi.getAll();
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
        orgApi.add(pojo);
        pojo = getOrgPojo(2, "increff2");
        orgApi.add(pojo);
        pojo = getOrgPojo(3, "increff2");
        try {
            orgApi.update(pojo);
        } catch (ApiException e) {
            List<OrganizationPojo> organizationPojoList = orgApi.getAll();
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
