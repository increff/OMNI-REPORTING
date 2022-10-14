package com.increff.omni.reporting.model.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ReportExpressionForm {

    @NotNull
    private Integer reportId;
    @NotEmpty
    private String expression;
    @NotEmpty
    private String expressionName;
}
