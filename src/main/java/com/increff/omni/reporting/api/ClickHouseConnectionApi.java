package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Log4j2
public class ClickHouseConnectionApi {

    @Autowired
    private ApplicationProperties properties;

    public Connection getConnection(String dbHost, String dbUsername, String dbPassword, Integer maxConnectionTime) throws ApiException {
        try {
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            DriverManager.setLoginTimeout(maxConnectionTime);
            Connection connection = DriverManager.getConnection(
                getDbUrl(dbHost), dbUsername, dbPassword);
            // Don't set readonly for ClickHouse HTTP connections
            // connection.setReadOnly(true);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error connecting to ClickHouse: " + e.getMessage());
        }
    }

    public PreparedStatement getStatement(Connection connection, Integer maxExecutionTime, String query, Integer fetchSize) throws ApiException {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setQueryTimeout(maxExecutionTime);
            statement.closeOnCompletion();
            return statement;
        } catch (SQLException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error preparing ClickHouse statement: " + e.getMessage());
        }
    }

    private String getDbUrl(String dbHost) {
        // Use standard JDBC URL - driver will auto-select HTTP on port 8123
        // Disable compression to avoid LZ4 dependency issues
        return "jdbc:clickhouse://" + dbHost + ":" + properties.getClickHouseDefaultPort() + "/default?compress=false";
    }
}
