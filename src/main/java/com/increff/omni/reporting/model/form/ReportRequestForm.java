package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ReportRequestForm {

    Integer reportId;
    Map<String, String> paramMap;

}
