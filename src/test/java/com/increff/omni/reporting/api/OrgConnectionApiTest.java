package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgConnectionPojo;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgSchemaPojo;
import static org.junit.Assert.assertEquals;

public class OrgConnectionApiTest extends AbstractTest {

    @Autowired
    private OrgConnectionApi api;

    @Test
    public void testMapOrgConnection() {
        OrgConnectionPojo pojo = getOrgConnectionPojo(1, 100001);
        api.map(pojo);
        List<OrgConnectionPojo> pojoList = api.selectAll();
        assertEquals(1, pojoList.size());
        assertEquals(1, pojoList.get(0).getOrgId().intValue());
        assertEquals(100001, pojoList.get(0).getConnectionId().intValue());
    }

    @Test
    public void testMapExistingOrgSchema() {
        OrgConnectionPojo pojo = getOrgConnectionPojo(1, 100001);
        api.map(pojo);
        pojo = getOrgConnectionPojo(2, 100001);
        api.map(pojo);
        pojo = getOrgConnectionPojo(1, 100002);
        api.map(pojo);
        List<OrgConnectionPojo> pojoList = api.selectAll();
        assertEquals(2, pojoList.size());
        assertEquals(1, pojoList.get(0).getOrgId().intValue());
        assertEquals(100002, pojoList.get(0).getConnectionId().intValue());
        assertEquals(2, pojoList.get(1).getOrgId().intValue());
        assertEquals(100001, pojoList.get(1).getConnectionId().intValue());
    }

    @Test
    public void testGetCheckByOrgId() throws ApiException {
        OrgConnectionPojo pojo = getOrgConnectionPojo(1, 100001);
        api.map(pojo);
        OrgConnectionPojo p = api.getCheckByOrgId(1);
        assertEquals(1, p.getOrgId().intValue());
        assertEquals(100001, p.getConnectionId().intValue());
    }

    @Test(expected = ApiException.class)
    public void testGetCheckByOrgIdWithException() throws ApiException {
        OrgConnectionPojo pojo = getOrgConnectionPojo(1, 100001);
        api.map(pojo);
        try {
            api.getCheckByOrgId(2);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No connection mapped for org : 2", e.getMessage());
            throw e;
        }
    }
}
