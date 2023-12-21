package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ConnectionApi;
import com.increff.omni.reporting.api.DBConnectionApi;
import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.service.encryption.EncryptionClient;
import com.increff.service.encryption.form.CryptoForm;
//import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.client.AppClientException;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
//import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getCryptoForm;

@Service
@Log4j2
@Setter
public class ConnectionDto extends AbstractDto {

    @Autowired
    private ConnectionApi api;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private DBConnectionApi dbConnectionApi;
    @Autowired
    private EncryptionClient encryptionClient;

    public ConnectionData add(ConnectionForm form) throws ApiException {
        checkValid(form);
        String password = encryptPassword(form, getUserId());
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo.setPassword(password);
        pojo = api.add(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public ConnectionData update(Integer id, ConnectionForm form) throws ApiException {
        checkValid(form);
        String password = encryptPassword(form, getUserId());
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo.setId(id);
        pojo.setPassword(password);
        api.saveAudit(id.toString(), AuditActions.EDIT_CONNECTION.toString(), "Edit Connection"
                , "Edit Connection", getUserName());
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public List<ConnectionData> selectAll() {
        List<ConnectionPojo> pojos = api.selectAll();
        return ConvertUtil.convert(pojos, ConnectionData.class);
    }

    public void testConnection(ConnectionForm form) throws ApiException {
        Connection connection = null;
        try {
            ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
            connection = dbConnectionApi.getConnection(pojo.getHost(), pojo.getUsername(),
                    pojo.getPassword(), properties.getMaxConnectionTime());
            PreparedStatement statement = dbConnectionApi.getStatement(connection,
                    properties.getLiveReportMaxExecutionTime(), "select version();",
                    properties.getResultSetFetchSize());
            ResultSet resultSet = statement.executeQuery();
            resultSet.close();
        } catch (SQLException e) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error connecting to database : " + e.getMessage());
        } finally {
            try {
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String encryptPassword(ConnectionForm connectionForm, Integer userId) throws ApiException {
        try {
            CryptoForm form = getCryptoForm(connectionForm.getPassword(), userId);
            return encryptionClient.encode(form).getValue();
        } catch (AppClientException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Failed to encrypt password : " + e.getMessage());
        }
    }
}
