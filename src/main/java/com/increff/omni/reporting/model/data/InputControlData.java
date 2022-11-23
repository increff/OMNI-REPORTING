package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class InputControlData {

    private Integer id;
    private String displayName;
    private String paramName;
    private InputControlScope scope;
    private InputControlType type;
    private String query;
    private List<String> values;
    private List<ValidationType> validationTypes = new ArrayList<>();
    private List<InputControlDataValue> options = new ArrayList<>();

    @Setter
    @Getter
    public static class InputControlDataValue {
        private String displayName;
        private String labelName;
    }
}
