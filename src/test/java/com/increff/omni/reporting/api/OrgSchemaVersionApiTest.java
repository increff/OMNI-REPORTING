package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.OrgSchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgSchemaPojo;
//import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrgSchemaVersionApiTest extends AbstractTest {

    @Autowired
    private OrgSchemaApi api;

    @Test
    public void testMapOrgSchema() {
        OrgSchemaVersionPojo pojo = getOrgSchemaPojo(1, 100001);
        api.map(pojo);
        List<OrgSchemaVersionPojo> pojoList = api.selectAll();
        assertEquals(1, pojoList.size());
        assertEquals(1, pojoList.get(0).getOrgId().intValue());
        assertEquals(100001, pojoList.get(0).getSchemaVersionId().intValue());
    }

    @Test
    public void testMapExistingOrgSchema() {
        OrgSchemaVersionPojo pojo = getOrgSchemaPojo(1, 100001);
        api.map(pojo);
        pojo = getOrgSchemaPojo(2, 100001);
        api.map(pojo);
        pojo = getOrgSchemaPojo(1, 100002);
        api.map(pojo);
        List<OrgSchemaVersionPojo> pojoList = api.selectAll();
        assertEquals(2, pojoList.size());
        assertEquals(1, pojoList.get(0).getOrgId().intValue());
        assertEquals(100002, pojoList.get(0).getSchemaVersionId().intValue());
        assertEquals(2, pojoList.get(1).getOrgId().intValue());
        assertEquals(100001, pojoList.get(1).getSchemaVersionId().intValue());
    }

    @Test
    public void testGetCheckByOrgId() throws ApiException{
        OrgSchemaVersionPojo pojo = getOrgSchemaPojo(1, 100001);
        api.map(pojo);
        OrgSchemaVersionPojo p = api.getCheckByOrgId(1);
        assertEquals(1, p.getOrgId().intValue());
        assertEquals(100001, p.getSchemaVersionId().intValue());
    }

    @Test
    void testGetCheckByOrgIdWithException() {
        OrgSchemaVersionPojo pojo = getOrgSchemaPojo(1, 100001);
        api.map(pojo);

        ApiException exception = assertThrows(ApiException.class, () -> {
            api.getCheckByOrgId(2);
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("No schema mapped for org : 2", exception.getMessage());
    }

}
