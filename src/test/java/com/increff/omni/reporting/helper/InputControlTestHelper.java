package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.omni.reporting.pojo.InputControlValuesPojo;

import java.util.ArrayList;
import java.util.List;

public class InputControlTestHelper {

    public static InputControlPojo getInputControlPojo(String displayName, String paramName, InputControlScope scope, InputControlType type) {
        InputControlPojo inputControlPojo = new InputControlPojo();
        inputControlPojo.setType(type);
        inputControlPojo.setDisplayName(displayName);
        inputControlPojo.setScope(scope);
        inputControlPojo.setParamName(paramName);
        return inputControlPojo;
    }

    public static InputControlQueryPojo getInputControlQueryPojo(String query, Integer controlId) {
        InputControlQueryPojo queryPojo = new InputControlQueryPojo();
        queryPojo.setQuery(query);
        queryPojo.setControlId(controlId);
        return queryPojo;
    }

    public static List<InputControlValuesPojo> getInputControlValuesPojo(List<String> values, Integer controlId) {
        List<InputControlValuesPojo> valuesPojoList = new ArrayList<>();
        values.forEach(v -> {
            InputControlValuesPojo valuesPojo = new InputControlValuesPojo();
            valuesPojo.setValue(v);
            valuesPojo.setControlId(controlId);
            valuesPojoList.add(valuesPojo);
        });
        return valuesPojoList;
    }
}

