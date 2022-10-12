package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ValidationGroupData {

    private String groupName;
    private ValidationType validationType;
    private Integer validationValue;
    private List<String> controls;
}
