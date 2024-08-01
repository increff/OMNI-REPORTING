package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConditionReplace {
    private List<Constraint> constraints;
}
