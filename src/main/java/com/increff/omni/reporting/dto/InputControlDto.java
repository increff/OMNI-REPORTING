package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.InputControlForm;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.FileUtil;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j
public class InputControlDto extends AbstractDto {

    @Autowired
    private InputControlApi api;

    @Autowired
    private FolderApi folderApi;

    @Autowired
    private InputControlFlowApi flowApi;

    @Autowired
    private ReportControlsApi reportControlsApi;

    @Autowired
    private OrgConnectionApi orgConnectionApi;

    @Autowired
    private ConnectionApi connectionApi;


    public InputControlData add(InputControlForm form) throws ApiException {
        validate(form);
        InputControlPojo pojo = ConvertUtil.convert(form, InputControlPojo.class);
        pojo = flowApi.add(pojo, form.getQuery(), form.getValues(), form.getReportId());
        return getInputControlDatas(Collections.singletonList(pojo)).get(0);
    }

    public InputControlData update(Integer id, InputControlForm form) throws ApiException {
        validate(form);
        InputControlPojo pojo = ConvertUtil.convert(form, InputControlPojo.class);
        pojo.setId(id);
        pojo = flowApi.update(pojo, form.getQuery(), form.getValues(), form.getReportId());
        return getInputControlDatas(Collections.singletonList(pojo)).get(0);
    }

    public List<InputControlData> selectAllGlobal() throws ApiException {
        List<InputControlPojo> pojos = api.getByScope(InputControlScope.GLOBAL);
        return getInputControlDatas(pojos);
    }

    public List<InputControlData> selectForReport(Integer reportId) throws ApiException {
        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = reportControlsPojos.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList());

        List<InputControlPojo> pojos = api.selectMultiple(controlIds);

        return getInputControlDatas(pojos);
    }

    private List<InputControlData> getInputControlDatas(List<InputControlPojo> pojos) throws ApiException {
        if (CollectionUtils.isEmpty(pojos))
            return new ArrayList<>();

        List<Integer> controlIds = pojos.stream()
                .map(InputControlPojo::getId).collect(Collectors.toList());

        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(getOrgId());
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
        //We need queries
        List<InputControlQueryPojo> queryPojos = api.selectControlQueries(controlIds);
        Map<Integer, String> controlToQueryMapping;
        if (queryPojos == null)
            controlToQueryMapping = new HashMap<>();
        else
            controlToQueryMapping = queryPojos.stream()
                    .collect(Collectors.toMap(InputControlQueryPojo::getControlId, InputControlQueryPojo::getQuery));

        //We need constants
        List<InputControlValuesPojo> valuesList = api.selectControlValues(controlIds);
        Map<Integer, List<String>> controlToValuesMapping;
        if (valuesList == null)
            controlToValuesMapping = new HashMap<>();
        else
            controlToValuesMapping = valuesList.stream()
                    .collect(Collectors.groupingBy(
                            InputControlValuesPojo::getControlId,
                            Collectors.mapping(InputControlValuesPojo::getValue, Collectors.toList())));

        return pojos.stream().map(p -> {
            InputControlData data = ConvertUtil.convert(p, InputControlData.class);
            data.setQuery(controlToQueryMapping.getOrDefault(p.getId(), null));
            if (Objects.nonNull(data.getQuery())) {
                try {
                    data.setQueryValues(flowApi.getValuesFromQuery(data.getQuery(), connectionPojo));
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            } else {
                List<String> values = controlToValuesMapping.getOrDefault(p.getId(), null);
                if (Objects.isNull(values) || values.isEmpty()) {
                    data.setQueryValues(new HashMap<>());
                } else {
                    Map<String, String> valuesMap = new HashMap<>();
                    values.forEach(m -> valuesMap.put(m, m));
                    data.setQueryValues(valuesMap);
                }
            }
            return data;
        }).collect(Collectors.toList());
    }

    private void validate(InputControlForm form) throws ApiException {
        checkValid(form);
        validateForControlType(form);
        validateForControlScope(form);
    }

    private void validateForControlScope(InputControlForm form) throws ApiException {
        if (form.getScope().equals(InputControlScope.LOCAL) && form.getReportId() == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report is mandatory for Local Scope Control");
    }

    private void validateForControlType(InputControlForm form) throws ApiException {
        switch (form.getType()) {
            case TEXT:
            case NUMBER:
            case DATE:
                if (form.getValues() != null || form.getQuery() != null)
                    throw new ApiException(ApiStatus.BAD_DATA, "For Text, Number and Date, neither query nor value is needed");
                break;

            case SINGLE_SELECT:
            case MULTI_SELECT:
                if (form.getValues() == null && form.getQuery() == null)
                    throw new ApiException(ApiStatus.BAD_DATA, "For Select, either query or value is mandatory");
                if (form.getValues() != null && form.getQuery() != null)
                    throw new ApiException(ApiStatus.BAD_DATA, "For Select, either query or value is mandatory");
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Unknown input control type");
        }
    }

}
