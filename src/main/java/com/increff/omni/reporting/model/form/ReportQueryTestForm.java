package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReportQueryTestForm extends ReportQueryForm{
    private String timezone = "UTC";
    private Map<String, List<String>> paramMap;
}
