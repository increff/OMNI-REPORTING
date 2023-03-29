package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class DBConnectionApi {

    @Autowired
    private ApplicationProperties properties;

    public Connection getConnection(String dbHost, String dbUsername, String dbPassword, Integer maxConnectionTime) throws ApiException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.setLoginTimeout(maxConnectionTime);
            Connection connection = DriverManager.getConnection(getDbUrl(dbHost), dbUsername, dbPassword);
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error connecting to the database " + e.getMessage());
        }
    }

    public PreparedStatement getStatement(Connection connection, Integer maxExecutionTime, String query,
                                          Integer fetchSize) throws ApiException {
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setQueryTimeout(maxExecutionTime);
            statement.closeOnCompletion();
//            statement.setFetchSize(fetchSize);
            return statement;
        } catch (SQLException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error connecting to the database " + e.getMessage());
        }
    }

    private String getDbUrl(String dbHost) {
        return "jdbc:mysql://" + dbHost + ":3306?useSSL=false";
    }
}
