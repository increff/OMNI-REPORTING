package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getReportControlPojo;

@Service
@Log4j
@Transactional(rollbackFor = ApiException.class)
public class InputControlFlowApi extends AbstractApi {

    @Autowired
    private InputControlApi api;

    @Autowired
    private DBConnectionApi dbConnectionApi;

    @Autowired
    private ReportControlsApi reportControlsApi;

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private FolderApi folderApi;

    @Autowired
    private ReportFlowApi reportFlowApi;

    @Autowired
    private ApplicationProperties properties;

    public InputControlPojo add(InputControlPojo pojo, String query, List<String> values,
                                Integer reportId) throws ApiException {

        if (pojo.getScope().equals(InputControlScope.LOCAL)) {
            validateLocalControl(reportId, pojo);
        }

        InputControlQueryPojo queryPojo = getQueryPojo(query);
        List<InputControlValuesPojo> valuesList = getValuesPojo(values);

        pojo = api.add(pojo, queryPojo, valuesList);

        if (pojo.getScope().equals(InputControlScope.LOCAL)) {
            ReportControlsPojo reportControlsPojo = getReportControlPojo(reportId, pojo.getId());
            reportControlsApi.add(reportControlsPojo);
            reportFlowApi.checkAndAddValidationGroup(reportControlsPojo);
        }
        return pojo;
    }

    public InputControlPojo update(InputControlPojo pojo, String query, List<String> values) throws ApiException {

        InputControlQueryPojo queryPojo = getQueryPojo(query);
        List<InputControlValuesPojo> valuesList = getValuesPojo(values);
        pojo = api.update(pojo, queryPojo, valuesList);
        return pojo;
    }

    public Map<String, String> getValuesFromQuery(String query, ConnectionPojo connectionPojo) {
        Connection connection = null;
        try {
            String fQuery = SqlCmd.getFinalQuery(new HashMap<>(), query, true);
            connection = dbConnectionApi.getConnection(connectionPojo.getHost(),
                    connectionPojo.getUsername(), connectionPojo.getPassword(),
                    properties.getMaxConnectionTime());
            PreparedStatement statement = dbConnectionApi.getStatement(connection,
                    properties.getLiveDataMaxExecutionTime(), fQuery, properties.getResultSetFetchSize());
            ResultSet resultSet = statement.executeQuery();
            return FileUtil.getMapFromResultSet(resultSet);
        } catch (Exception e) {
            log.error("Error while getting input control values : ", e);
        } finally {
            try {
                if (Objects.nonNull(connection)) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    private InputControlQueryPojo getQueryPojo(String query) {
        if (StringUtil.isEmpty(query))
            return null;
        InputControlQueryPojo pojo = new InputControlQueryPojo();
        pojo.setQuery(query);
        return pojo;
    }

    private List<InputControlValuesPojo> getValuesPojo(List<String> values) {
        if (CollectionUtils.isEmpty(values))
            return new ArrayList<>();

        return values.stream().map(v -> {
            InputControlValuesPojo pojo = new InputControlValuesPojo();
            pojo.setValue(v);
            return pojo;
        }).collect(Collectors.toList());
    }

    private void validateLocalControl(Integer reportId, InputControlPojo pojo) throws ApiException {
        ReportPojo reportPojo = reportApi.getCheck(reportId);
        if(!pojo.getSchemaVersionId().equals(reportPojo.getSchemaVersionId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report Schema version and input control schema version not " +
                    "matching");
        // Validating if any other control exists with same display or param name
        List<ReportControlsPojo> existingPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = existingPojos.stream().map(ReportControlsPojo::getControlId)
                .collect(Collectors.toList());

        List<InputControlPojo> controlPojos = api.selectByIds(controlIds);

        List<InputControlPojo> duplicate = controlPojos.stream()
                .filter(i -> (i.getDisplayName().equals(pojo.getDisplayName()) ||
                        i.getParamName().equals(pojo.getParamName())))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(duplicate))
            throw new ApiException(ApiStatus.BAD_DATA, "Another input control present with same display name" +
                    " or param name");
    }

}
