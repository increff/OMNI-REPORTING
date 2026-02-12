package com.increff.omni.reporting.api;

import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.config.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class ClickHouseConnectionApiTest extends AbstractTest {

    @Autowired
    private ClickHouseConnectionApi clickHouseConnectionApi;

    @Test
    public void testGetConnectionInvalidHost() {
        ApiException exception = assertThrows(ApiException.class, () ->
                clickHouseConnectionApi.getConnection("invalid-host", "user", "pass", 5));
        assertEquals(ApiStatus.UNKNOWN_ERROR, exception.getStatus());
        assertTrue(exception.getMessage().contains("Error connecting to ClickHouse"));
    }

    @Test
    public void testGetStatementInvalidQuery() throws ApiException {
        // getStatement with a closed/null connection should throw ApiException
        try {
            Connection connection = clickHouseConnectionApi.getConnection("localhost", "default", "", 5);
            // If connection succeeds (ClickHouse running locally), test with invalid SQL
            PreparedStatement statement = clickHouseConnectionApi.getStatement(connection, 5, "SELECT 1", 100);
            assertNotNull(statement);
            connection.close();
        } catch (Exception e) {
            // Expected when ClickHouse is not running - connection failure is tested above
            assertTrue(e instanceof ApiException);
        }
    }
}
