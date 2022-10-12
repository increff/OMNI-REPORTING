package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.mysql.jdbc.Driver;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import com.nextscm.commons.spring.server.DtoHelper;
import lombok.extern.log4j.Log4j;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j
public abstract class AbstractDto extends AbstractDtoApi {

    protected static int getOrgId() {
        return getPrincipal().getDomainId();
    }

    protected static int getUserId() {
        return getPrincipal().getId();
    }

    private static UserPrincipal getPrincipal() {
        return SecurityUtil.getPrincipal();
    }

    protected Map<String, String> getInputParamValueMap(ConnectionPojo connectionPojo, String query) {
        Map<String, String> keyValueMap = new HashMap<>();
        try {
            DriverManager.registerDriver(new Driver());
            Connection connection = DriverManager.getConnection(connectionPojo.getHost(), connectionPojo.getUsername(), connectionPojo.getPassword());
            Statement statement = connection.createStatement();
            // Only 2 columns will be there in query
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                keyValueMap.put(rs.getString(1),rs.getString(2));
            }
        } catch (SQLException e) {
            log.error("Error while getting input param values from query : " + query, e);
        }
        return keyValueMap;
    }
}
