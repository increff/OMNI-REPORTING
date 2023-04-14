package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InputControlFilterData {

    private String paramName;
    private String displayName;
    private InputControlType type;
    private List<String> values;
}
