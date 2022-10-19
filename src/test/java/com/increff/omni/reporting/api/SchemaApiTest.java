package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.assertEquals;

public class SchemaApiTest extends AbstractTest {

    @Autowired
    private SchemaApi schemaApi;

    @Test
    public void testAddSchema() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaApi.add(pojo);
        pojo = schemaApi.getCheck(pojo.getId());
        assertEquals("9.0.1", pojo.getName());
    }

    @Test(expected = ApiException.class)
    public void testAddSchemaDuplicateName() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaApi.add(pojo);
        try {
            schemaApi.add(pojo);
        } catch (ApiException e) {
            pojo = schemaApi.getCheck(pojo.getId());
            assertEquals("9.0.1", pojo.getName());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Schema already present with name : 9.0.1", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testGetCheckNoOrg() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaApi.add(pojo);
        try {
            schemaApi.getCheck(pojo.getId() + 1);
        } catch (ApiException e) {
            pojo = schemaApi.getCheck(pojo.getId());
            assertEquals("9.0.1", pojo.getName());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No schema present with id : " + (pojo.getId() + 1), e.getMessage());
            throw e;
        }
    }

    @Test
    public void testGetAll() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaApi.add(pojo);
        pojo = getSchemaPojo("9.0.2");
        schemaApi.add(pojo);
        List<SchemaVersionPojo> pojoList = schemaApi.selectAll();
        assertEquals(2, pojoList.size());
        assertEquals("9.0.1", pojoList.get(0).getName());
        assertEquals("9.0.2", pojoList.get(1).getName());
    }
}
