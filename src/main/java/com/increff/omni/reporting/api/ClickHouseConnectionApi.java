package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.ClickHouseDatabaseMappingDao;
import com.increff.omni.reporting.pojo.ClickHouseDatabaseMappingPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;

@Service
@Log4j2
@Transactional(rollbackFor = ApiException.class)
public class ClickHouseConnectionApi {

    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private ClickHouseDatabaseMappingDao clickHouseDatabaseMappingDao;

    public Connection getConnection(String dbHost, String dbUsername, String dbPassword, String database) throws ApiException {
        try {
            Class.forName("com.clickhouse.jdbc.ClickHouseDriver");
            DriverManager.setLoginTimeout(properties.getClickHouseConnectTimeoutSec());
            Connection connection = DriverManager.getConnection(
                getDbUrl(dbHost, database), dbUsername, dbPassword);
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

    public void addDatabaseMapping(ClickHouseDatabaseMappingPojo pojo) {
        clickHouseDatabaseMappingDao.persist(pojo);
    }

    public ClickHouseDatabaseMappingPojo getDatabaseMapping(Integer connectionId) throws ApiException {
        List<ClickHouseDatabaseMappingPojo> mappings = clickHouseDatabaseMappingDao.selectByConnectionId(connectionId);
        if (mappings.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "No database mapping found for ClickHouse connectionId: " + connectionId);
        }
        return mappings.get(0);
    }

    public void updateDatabaseMapping(ClickHouseDatabaseMappingPojo pojo) {
        clickHouseDatabaseMappingDao.update(pojo);
    }

    public String getDatabaseByConnectionId(Integer connectionId) throws ApiException {
        List<ClickHouseDatabaseMappingPojo> mappings = clickHouseDatabaseMappingDao.selectByConnectionId(connectionId);
        if (mappings.isEmpty()) {
            throw new ApiException(ApiStatus.BAD_DATA, "No database mapping found for ClickHouse connectionId: " + connectionId);
        }
        return mappings.get(0).getDatabaseName();
    }

    private String getDbUrl(String dbHost, String database) {
        return "jdbc:clickhouse://" + dbHost + ":" + properties.getClickHouseDefaultPort() + "/" + database + "?compress=false";
    }
}
