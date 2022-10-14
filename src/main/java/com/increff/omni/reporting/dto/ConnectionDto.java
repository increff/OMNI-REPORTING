package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ConnectionApi;
import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.model.SqlParams;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Log4j
public class ConnectionDto extends AbstractDtoApi {

    @Autowired
    private ConnectionApi api;
    @Autowired
    private FolderApi folderApi;

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
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public List<ConnectionData> selectAll() {
        List<ConnectionPojo> pojos = api.selectAll();
        return ConvertUtil.convert(pojos, ConnectionData.class);
    }

    public void testConnection(Integer id) throws ApiException {
        ConnectionPojo pojo = api.getCheck(id);
        String result = execute(pojo, "select version();");
        if (result.contains("ERROR")) {
            throw new ApiException(ApiStatus.BAD_DATA, "Database could not be connected");
        }
    }

    public String execute(ConnectionPojo connectionPojo, String query) throws ApiException {
        log.debug("Executing the query = " + query);
        String result;
        File file = null;
        File errFile = null;
        try {
            file = folderApi.getFile("test-db-success.tsv");
            errFile = folderApi.getFile("test-db-err.txt");
            SqlParams sqlp = CommonDtoHelper.getSqlParams(connectionPojo, query, file, errFile);
            SqlCmd.processQuery(sqlp);
            result = FileUtils.readFileToString(file, "utf-8");
            log.debug("Test File created");
        } catch (IOException | ApiException e) {
            log.error("Error in testing connection ", e);
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Error in testing if database is read-only");
        } finally {
            FileUtil.delete(file);
            FileUtil.delete(errFile);
        }
        return result;
    }
}
