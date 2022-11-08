package com.increff.omni.reporting.flow;

import com.increff.commons.sheet.DataRow;
import com.increff.commons.sheet.TsvFile;
import com.increff.omni.reporting.api.FolderApi;
import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    private ReportControlsApi reportControlsApi;

    @Autowired
    private ReportApi reportApi;

    @Autowired
    private FolderApi folderApi;

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
            reportControlsApi.add(getReportControlPojo(reportId, pojo.getId()));
        }
        return pojo;
    }

    public InputControlPojo update(InputControlPojo pojo, String query, List<String> values) throws ApiException {

        InputControlQueryPojo queryPojo = getQueryPojo(query);
        List<InputControlValuesPojo> valuesList = getValuesPojo(values);
        pojo = api.update(pojo, queryPojo, valuesList);
        return pojo;
    }

    public Map<String, String> getValuesFromQuery(String query, ConnectionPojo connectionPojo) throws ApiException {
        File file = null;
        File errFile = null;
        try {
            String fileName = UUID.randomUUID().toString();
            file = folderApi.getFile(fileName + ".tsv");
            errFile = folderApi.getFile(fileName + "-err.txt");
            SqlParams sqlp = CommonDtoHelper.getSqlParams(connectionPojo, query, file, errFile, properties.getMaxExecutionTime());
            SqlCmd.processQuery(sqlp);
            return getMapFromTsv(file);
        } catch (ApiException e) {
            log.error("Error while getting input control values ", e);
        } finally {
            FileUtil.delete(file);
            FileUtil.delete(errFile);
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
        reportApi.getCheck(reportId);

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

    private Map<String, String> getMapFromTsv(File file) {
        try (FileInputStream in = new FileInputStream(file)){
            Map<String, String> fMap = new HashMap<>();
            TsvFile tsvFile = new TsvFile();
            tsvFile.read(in);
            ArrayList<DataRow> list = tsvFile.getData();
            for (DataRow d : list) {
                fMap.put(d.getValue(0), d.getValue(1));
            }
            return fMap;
        } catch (Exception e) {
            log.error("Error while converting file to map ", e);
        }
        return new HashMap<>();
    }

}
