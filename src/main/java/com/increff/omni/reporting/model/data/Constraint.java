package com.increff.omni.reporting.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Constraint {
    private List<Condition> conditions;
    private String query;
}
