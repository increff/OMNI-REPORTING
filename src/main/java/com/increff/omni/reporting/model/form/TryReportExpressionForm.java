package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

@Getter
@Setter
public class TryReportExpressionForm {
    @NotEmpty
    private String expression;
    private Map<String, String> paramMap;
}
