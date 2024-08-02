package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConditionReplace {
    private List<Constraint> constraints;
    int version = 1; // kept for future use in case syntax/object structure changes
}
