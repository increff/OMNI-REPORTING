//package com.increff.omni.reporting.api;
//
//import com.increff.omni.reporting.config.AbstractTest;
//import com.increff.omni.reporting.pojo.ConnectionPojo;
//import com.nextscm.commons.spring.common.ApiException;
//import com.nextscm.commons.spring.common.ApiStatus;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//
//import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionPojo;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//
//public class ConnectionApiTest extends AbstractTest {
//
//    @Autowired
//    private ConnectionApi api;
//
//    @Test
//    public void testAddConnection() throws ApiException {
//        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
//                , "db.password");
//        pojo = api.add(pojo);
//        assertNotNull(pojo);
//        assertEquals("dev-db.increff.com", pojo.getHost());
//        assertEquals("Dev DB", pojo.getName());
//        assertEquals("db.user", pojo.getUsername());
//        assertEquals("db.password", pojo.getPassword());
//    }
//
//    @Test(expected = ApiException.class)
//    public void testAddConnectionAlreadyExists() throws ApiException {
//        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
//                , "db.password");
//        api.add(pojo);
//        try {
//            ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
//                    , "db.password2");
//            api.add(pojo2);
//        } catch (ApiException e) {
//            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
//            assertEquals("Connection with same name already present", e.getMessage());
//            throw e;
//        }
//    }
//
//    @Test
//    public void testUpdateConnection() throws ApiException {
//        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
//                , "db.password");
//        api.add(pojo);
//        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
//                , "db.password2");
//        pojo2.setId(pojo.getId());
//        api.update(pojo2);
//        List<ConnectionPojo> pojoList = api.selectAll();
//        assertEquals(1, pojoList.size());
//        assertEquals("dev-db-2.increff.com", pojoList.get(0).getHost());
//        assertEquals("Dev DB", pojoList.get(0).getName());
//        assertEquals("db.user2", pojoList.get(0).getUsername());
//        assertEquals("db.password2", pojoList.get(0).getPassword());
//    }
//
//    @Test(expected = ApiException.class)
//    public void testUpdateConnectionNameExistsWithOtherId() throws ApiException {
//        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
//                , "db.password");
//        api.add(pojo);
//        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB", "db.user2"
//                , "db.password2");
//        pojo2.setId(pojo.getId() + 1);
//        try {
//            api.update(pojo2);
//        } catch (ApiException e) {
//            List<ConnectionPojo> pojoList = api.selectAll();
//            assertEquals(1, pojoList.size());
//            assertEquals("dev-db.increff.com", pojoList.get(0).getHost());
//            assertEquals("Dev DB", pojoList.get(0).getName());
//            assertEquals("db.user", pojoList.get(0).getUsername());
//            assertEquals("db.password", pojoList.get(0).getPassword());
//            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
//            assertEquals("Connection with same name already present", e.getMessage());
//            throw e;
//        }
//    }
//
//    @Test
//    public void testUpdateConnectionWithDifferentName() throws ApiException {
//        ConnectionPojo pojo = getConnectionPojo("dev-db.increff.com", "Dev DB", "db.user"
//                , "db.password");
//        api.add(pojo);
//        ConnectionPojo pojo2 = getConnectionPojo("dev-db-2.increff.com", "Dev DB 2", "db.user2"
//                , "db.password2");
//        pojo2.setId(pojo.getId());
//        api.update(pojo2);
//        List<ConnectionPojo> pojoList = api.selectAll();
//        assertEquals(1, pojoList.size());
//        assertEquals("dev-db-2.increff.com", pojoList.get(0).getHost());
//        assertEquals("Dev DB 2", pojoList.get(0).getName());
//        assertEquals("db.user2", pojoList.get(0).getUsername());
//        assertEquals("db.password2", pojoList.get(0).getPassword());
//    }
//
//}
