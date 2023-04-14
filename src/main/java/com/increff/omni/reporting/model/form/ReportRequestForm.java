package com.increff.omni.reporting.model.form;

import com.increff.commons.queryexecutor.constants.FileFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ReportRequestForm {

    private Integer reportId;
    private String timezone = "UTC";
    private FileFormat fileFormat;
    private Map<String, List<String>> paramMap;

}
