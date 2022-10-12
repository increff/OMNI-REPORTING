package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.form.InputControlForm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class InputControlData extends InputControlForm {

    private Integer id;
    private String displayName;
    private String paramName;
    private InputControlScope scope;
    private InputControlType type;
    private String query;
    private List<String> values;
    private Map<String, String> queryValues;

}
