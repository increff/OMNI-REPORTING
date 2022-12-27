package com.increff.omni.reporting.model.constants;

import lombok.Getter;

@Getter
public enum QueryOperator {
    LT("<"), GT(">"), LE("<="), GE(">="), NE("!="), EQS("="), INS("in");

    private String value;

    QueryOperator(String operator) {
        this.value = operator;
    }

    public static QueryOperator valueOfLabel(String value) {
        for (QueryOperator o : values()) {
            if (o.value.equals(value)) {
                return o;
            }
        }
        return null;
    }
}
