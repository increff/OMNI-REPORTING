package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.InputControlForm;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQuery;
import com.increff.omni.reporting.pojo.InputControlValues;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InputControlDto extends AbstractDtoApi {

    @Autowired
    private InputControlApi api;

    @Autowired
    private InputControlFlowApi flowApi;

    @Autowired
    private ReportControlsApi reportControlsApi;

    public InputControlData add(InputControlForm form) throws ApiException {
        validate(form);
        InputControlPojo pojo = ConvertUtil.convert(form, InputControlPojo.class);
        pojo = flowApi.add(pojo, form.getQuery(), form.getValues(), form.getReportId(), form.getValidationType());
        return getInputControlData(pojo);
    }

    public List<InputControlData> selectAllGlobal(){
        List<InputControlPojo> pojos = api.getByScope(InputControlScope.GLOBAL);
        return getInputControlDatas(pojos);
    }

    public List<InputControlData> selectForReport(Integer reportId){
        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = reportControlsPojos.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList());

        List<InputControlPojo> pojos = api.selectMultiple(controlIds);

        return getInputControlDatas(pojos);
    }

    private InputControlData getInputControlData(InputControlPojo pojo) {
        return getInputControlDatas(Collections.singletonList(pojo)).get(0);
    }

    private List<InputControlData> getInputControlDatas(List<InputControlPojo> pojos){
        if(CollectionUtils.isEmpty(pojos))
            return new ArrayList<>();

        List<Integer> controlIds = pojos.stream()
                .map(InputControlPojo::getId).collect(Collectors.toList());

        //We need queries
        List<InputControlQuery> queryPojos = api.selectControlQueries(controlIds);
        Map<Integer, String> controlToQueryMapping;
        if(queryPojos == null)
            controlToQueryMapping = new HashMap<>();
        else
            controlToQueryMapping = queryPojos.stream()
                .collect(Collectors.toMap(InputControlQuery::getControlId, InputControlQuery::getQuery));

        //We need constants
        List<InputControlValues> valuesList = api.selectControlValues(controlIds);
        Map<Integer, List<String>> controlToValuesMapping;
        if(valuesList == null)
            controlToValuesMapping = new HashMap<>();
        else
            controlToValuesMapping = valuesList.stream()
                .collect(Collectors.groupingBy(
                        InputControlValues::getControlId,
                        Collectors.mapping(InputControlValues::getValue, Collectors.toList())));

        return pojos.stream().map(p -> {
            InputControlData data = ConvertUtil.convert(p, InputControlData.class);
            data.setQuery(controlToQueryMapping.getOrDefault(p.getId(), null));
            data.setValues(controlToValuesMapping.getOrDefault(p.getId(), null));
            return data;
        }).collect(Collectors.toList());
    }

    private void validate(InputControlForm form) throws ApiException {
        checkValid(form);
        validateForControlType(form);
        validateForControlScope(form);
    }

    private void validateForControlScope(InputControlForm form) throws ApiException {
        if(form.getScope().equals(InputControlScope.LOCAL) && form.getReportId() == null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report is mandatory for Local Scope Control");
    }

    private void validateForControlType(InputControlForm form) throws ApiException {
        switch (form.getType()){
            case TEXT:
            case NUMBER:
            case DATE:
                break;

            case SINGLE_SELECT:
            case MULTI_SELECT:
                if(form.getValues() == null && form.getQuery() == null)
                    throw new ApiException(ApiStatus.BAD_DATA, "For Select, either query or value is mandatory");
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Unknown input control type");
        }
    }

}
