package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ConditionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Condition {
    private ConditionType type;
    private List<String> foreignKeys;
    private List<String> values;
}
