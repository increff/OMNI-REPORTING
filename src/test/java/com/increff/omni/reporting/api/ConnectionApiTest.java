package com.increff.omni.reporting.api;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(AbstractTest.class)
public class ConnectionApiTest {

    @Autowired
    private ConnectionApi api;

    @Test
    public void testAddConnection() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password");
        pojo = api.add(pojo);
        assertNotNull(pojo);
        assertEquals("dev-db.increff.com", pojo.getHost());
        assertEquals("Dev DB", pojo.getName());
        assertEquals("db.user", pojo.getUsername());
        assertEquals("db.password", pojo.getPassword());
    }

    @Test
    public void testAddConnectionAlreadyExists() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password");
        api.add(pojo);
        try {
            ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
                    , "db.password2");
            api.add(pojo2);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Connection with same name already present", e.getMessage());
        }
    }

    @Test
    public void testUpdateConnection() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password");
        api.add(pojo);
        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
                , "db.password2");
        pojo2.setId(pojo.getId());
        api.update(pojo2);
        List<ConnectionPojo> pojoList = api.selectAll();
        assertEquals(1, pojoList.size());
        assertEquals("dev-db-2.increff.com", pojoList.get(0).getHost());
        assertEquals("Dev DB", pojoList.get(0).getName());
        assertEquals("db.user2", pojoList.get(0).getUsername());
        assertEquals("db.password2", pojoList.get(0).getPassword());
    }

    @Test
    public void testUpdateConnectionNameExistsWithOtherId() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password");
        api.add(pojo);
        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
                , "db.password2");
        pojo2.setId(pojo.getId() + 1);
        try {
            api.update(pojo2);
        } catch (ApiException e) {
            List<ConnectionPojo> pojoList = api.selectAll();
            assertEquals(1, pojoList.size());
            assertEquals("dev-db.increff.com", pojoList.get(0).getHost());
            assertEquals("Dev DB", pojoList.get(0).getName());
            assertEquals("db.user", pojoList.get(0).getUsername());
            assertEquals("db.password", pojoList.get(0).getPassword());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Connection with same name already present", e.getMessage());
        }
    }

    @Test
    public void testUpdateConnectionWithDifferentName() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
                , "db.password");
        api.add(pojo);
        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB 2", "db.user2"
                , "db.password2");
        pojo2.setId(pojo.getId());
        api.update(pojo2);
        List<ConnectionPojo> pojoList = api.selectAll();
        assertEquals(1, pojoList.size());
        assertEquals("dev-db-2.increff.com", pojoList.get(0).getHost());
        assertEquals("Dev DB 2", pojoList.get(0).getName());
        assertEquals("db.user2", pojoList.get(0).getUsername());
        assertEquals("db.password2", pojoList.get(0).getPassword());
    }

}
