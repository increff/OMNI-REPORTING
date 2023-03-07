package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ConnectionApi;
import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.AuditActions;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Log4j
public class ConnectionDto extends AbstractDto {

    @Autowired
    private ConnectionApi api;
    @Autowired
    private FolderApi folderApi;
    @Autowired
    private ApplicationProperties properties;

    public ConnectionData add(ConnectionForm form) throws ApiException {
        checkValid(form);
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo = api.add(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public ConnectionData update(Integer id, ConnectionForm form) throws ApiException {
        checkValid(form);
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo.setId(id);
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
        String result = execute(form);
        if (result.contains("ERROR")) {
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Database could not be connected");
        }
    }

    private String execute(ConnectionForm form) throws ApiException {
        String result;
        File file = null;
        File errFile = null;
        try {
            file = folderApi.getFile("test-db-success.tsv");
            errFile = folderApi.getFile("test-db-err.txt");
            ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
            SqlParams sqlp = CommonDtoHelper.getSqlParams(pojo, "select version();", file, errFile, properties.getMaxExecutionTime());
            SqlCmd.processQuery(sqlp, properties.getMaxExecutionTime());
            result = FileUtils.readFileToString(file, "utf-8");
            log.debug("Test File created");
        } catch (IOException | InterruptedException | ApiException e) {
            log.error("Error in testing connection ", e);
            try {
                assert errFile != null;
                result = FileUtils.readFileToString(errFile, "utf-8");
            } catch (IOException ex) {
                throw new ApiException(ApiStatus.UNKNOWN_ERROR, ex.getMessage());
            }
        } finally {
            FileUtil.delete(file);
            FileUtil.delete(errFile);
        }
        return result;
    }
}
