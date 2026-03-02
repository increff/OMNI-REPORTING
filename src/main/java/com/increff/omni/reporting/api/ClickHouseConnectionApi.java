package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;

@Service
@Log4j2
@Transactional(rollbackFor = ApiException.class)
public class ClickHouseConnectionApi {

    @Autowired
    private ApplicationProperties properties;

    public Connection getConnection(String connectionString, String dbUsername, String dbPassword) throws ApiException {
        try {
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            DriverManager.setLoginTimeout(properties.getClickHouseConnectTimeoutSec());
            Connection connection = DriverManager.getConnection(connectionString, dbUsername, dbPassword);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error connecting to ClickHouse: " + e.getMessage());
        }
    }

    public PreparedStatement getStatement(Connection connection, Integer maxExecutionTime, String query, Integer fetchSize) throws ApiException {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setQueryTimeout(maxExecutionTime);
            statement.setFetchSize(fetchSize);
            statement.closeOnCompletion();
            return statement;
        } catch (SQLException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error preparing ClickHouse statement: " + e.getMessage());
        }
    }

}
