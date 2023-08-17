package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.flow.InputControlFlowApi;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.InputControlForm;
import com.increff.omni.reporting.model.form.InputControlUpdateForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.sortBasedOnReportControlMappedTime;
import static com.increff.omni.reporting.dto.CommonDtoHelper.updateValidationTypes;

@Service
@Log4j
@Setter
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
    private ReportValidationGroupApi reportValidationGroupApi;

    @Autowired
    private OrgConnectionApi orgConnectionApi;

    @Autowired
    private ConnectionApi connectionApi;

    @Autowired
    private SchemaVersionApi schemaVersionApi;

    public InputControlData add(InputControlForm form) throws ApiException {
        validate(form);
        InputControlPojo pojo = ConvertUtil.convert(form, InputControlPojo.class);
        pojo = flowApi.add(pojo, form.getQuery(), form.getValues(), form.getReportId());
        return getInputControlDatas(Collections.singletonList(pojo), getOrgId()).get(0);
    }

    public InputControlData update(Integer id, InputControlUpdateForm form) throws ApiException {
        validateForEdit(form);
        InputControlPojo pojo = ConvertUtil.convert(form, InputControlPojo.class);
        pojo.setId(id);
        pojo = flowApi.update(pojo, form.getQuery(), form.getValues());
        return getInputControlDatas(Collections.singletonList(pojo), getOrgId()).get(0);
    }

    public InputControlData getById(Integer id) throws ApiException {
        InputControlPojo pojo = api.getCheck(id);
        return getInputControlDatas(Collections.singletonList(pojo), getOrgId()).get(0);
    }

    public List<InputControlData> selectAllGlobal(Integer schemaVersionId) throws ApiException {
        List<InputControlPojo> pojos = api.getByScopeAndSchema(InputControlScope.GLOBAL, schemaVersionId);
        return getInputControlDatas(pojos, getOrgId());
    }

    public List<InputControlData> selectForReport(Integer reportId) throws ApiException {
        return selectForReport(reportId, getOrgId());
    }

    public List<InputControlData> selectForReport(Integer reportId, Integer orgId) throws ApiException {
        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(reportId);
        List<ReportValidationGroupPojo> validationGroupPojoList = reportValidationGroupApi.getByReportId(reportId);

        List<Integer> controlIds = reportControlsPojos.stream()
                .map(ReportControlsPojo::getControlId).collect(Collectors.toList());

        List<InputControlPojo> pojos = api.selectByIds(controlIds);

        List<InputControlData> inputControlDataList = getInputControlDatas(pojos, orgId);
        sortBasedOnReportControlMappedTime(inputControlDataList, reportControlsPojos);
        updateValidationTypes(inputControlDataList, validationGroupPojoList, reportControlsPojos);
        return inputControlDataList;
    }

    private List<InputControlData> getInputControlDatas(List<InputControlPojo> pojos, Integer orgId) throws ApiException {
        if (CollectionUtils.isEmpty(pojos))
            return new ArrayList<>();

        List<Integer> controlIds = pojos.stream()
                .map(InputControlPojo::getId).collect(Collectors.toList());

        OrgConnectionPojo orgConnectionPojo = orgConnectionApi.getCheckByOrgId(orgId);
        ConnectionPojo connectionPojo = connectionApi.getCheck(orgConnectionPojo.getConnectionId());
        String password = getDecryptedPassword(connectionPojo.getPassword());
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
        List<InputControlData> dataList = new ArrayList<>();
        for(InputControlPojo p : pojos) {
            SchemaVersionPojo schemaVersionPojo = schemaVersionApi.getCheck(p.getSchemaVersionId());
            dataList.add(getDataFromPojo(p, controlToValuesMapping, controlToQueryMapping, connectionPojo,
                    schemaVersionPojo, password));
        }
        return dataList;
    }

    private InputControlData getDataFromPojo(InputControlPojo p, Map<Integer, List<String>> controlToValuesMapping
            , Map<Integer, String> controlToQueryMapping, ConnectionPojo connectionPojo,
                                             SchemaVersionPojo schemaVersionPojo, String password) throws ApiException {
        InputControlData data = ConvertUtil.convert(p, InputControlData.class);
        data.setSchemaVersionName(schemaVersionPojo.getName());
        data.setQuery(controlToQueryMapping.getOrDefault(p.getId(), null));
        data.setValues(controlToValuesMapping.getOrDefault(p.getId(), null));
        if (!StringUtil.isEmpty(data.getQuery())) {
            setInputControlOptions(data, flowApi.getValuesFromQuery(data.getQuery(), connectionPojo, password));
        } else {
            List<String> values = controlToValuesMapping.getOrDefault(p.getId(), null);
            if (!CollectionUtils.isEmpty(values)) {
                Map<String, String> valuesMap = new LinkedHashMap<>();
                values.forEach(m -> valuesMap.put(m, m));
                setInputControlOptions(data, valuesMap);
            }
        }
        return data;
    }

    private void setInputControlOptions(InputControlData data, Map<String, String> values) {
        values.keySet().forEach(k -> {
            InputControlData.InputControlDataValue value = new InputControlData.InputControlDataValue();
            value.setLabelName(k);
            value.setDisplayName(values.get(k));
            data.getOptions().add(value);
        });
    }

    private void validate(InputControlForm form) throws ApiException {
        checkValid(form);
        validateForControlType(form.getQuery(), form.getType(), form.getValues());
        validateForControlScope(form);
    }

    private void validateForEdit(InputControlUpdateForm form) throws ApiException {
        checkValid(form);
        validateForControlType(form.getQuery(), form.getType(), form.getValues());
    }

    private void validateForControlScope(InputControlForm form) throws ApiException {
        if (form.getScope().equals(InputControlScope.LOCAL) && Objects.isNull(form.getReportId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Report is mandatory for Local Scope Control");
    }

    private void validateForControlType(String query, InputControlType type, List<String> values) throws ApiException {
        switch (type) {
            case TEXT:
            case MULTI_TEXT:
            case NUMBER:
            case DATE:
            case DATE_TIME:
                if (!values.isEmpty() || !StringUtil.isEmpty(query))
                    throw new ApiException(ApiStatus.BAD_DATA, "For Text, Number and Date, neither query nor value is" +
                            " needed");
                break;

            case SINGLE_SELECT:
            case MULTI_SELECT:
            case ACCESS_CONTROLLED_MULTI_SELECT:
                if (values.isEmpty() && StringUtil.isEmpty(query))
                    throw new ApiException(ApiStatus.BAD_DATA, "For Select, either query or value is mandatory");
                if (!values.isEmpty() && !StringUtil.isEmpty(query))
                    throw new ApiException(ApiStatus.BAD_DATA, "For Select, either query or value one is mandatory");
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Unknown input control type");
        }
    }

}
