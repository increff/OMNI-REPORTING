package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
//import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SchemaVersionApiTest extends AbstractTest {

    @Autowired
    private SchemaVersionApi schemaVersionApi;

    @Test
    public void testAddSchema() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(pojo);
        pojo = schemaVersionApi.getCheck(pojo.getId());
        assertEquals("9.0.1", pojo.getName());
    }

    @Test
    public void testAddSchemaDuplicateName() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(pojo);
        ApiException exception = assertThrows(ApiException.class, () -> {
            schemaVersionApi.add(pojo);
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Schema already present with name : 9.0.1", exception.getMessage());

        SchemaVersionPojo retrievedPojo = schemaVersionApi.getCheck(pojo.getId());
        assertEquals("9.0.1", retrievedPojo.getName());
    }

    @Test
    public void testGetCheckNoOrg() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(pojo);
        ApiException exception = assertThrows(ApiException.class, () -> {
            schemaVersionApi.getCheck(pojo.getId() + 1);
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("No schema present with id : " + (pojo.getId() + 1), exception.getMessage());

        SchemaVersionPojo retrievedPojo = schemaVersionApi.getCheck(pojo.getId());
        assertEquals("9.0.1", retrievedPojo.getName());
    }

    @Test
    public void testGetAll() throws ApiException {
        SchemaVersionPojo pojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(pojo);
        pojo = getSchemaPojo("9.0.2");
        schemaVersionApi.add(pojo);
        List<SchemaVersionPojo> pojoList = schemaVersionApi.selectAll();
        assertEquals(2, pojoList.size());
        assertEquals("9.0.1", pojoList.get(0).getName());
        assertEquals("9.0.2", pojoList.get(1).getName());
    }
}
