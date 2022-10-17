package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.CustomReportAccessForm;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomReportAccessData extends CustomReportAccessForm {
    private Integer id;
    private String reportName;
    private String orgName;
}
