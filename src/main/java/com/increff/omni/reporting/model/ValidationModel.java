package com.increff.omni.reporting.model;

import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ValidationModel {

    private Integer reportId;
    private ValidationType type;
    private Map<String, String> items;

}
