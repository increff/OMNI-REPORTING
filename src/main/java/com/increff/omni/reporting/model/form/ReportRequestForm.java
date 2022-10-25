package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ReportRequestForm {

    private Integer reportId;
    private String timezone = "UTC";
    private Map<String, String> paramMap;

}
