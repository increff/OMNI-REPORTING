package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "reportExpression")
public class ReportExpressionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_expression", pkColumnValue = "report_expression", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_expression")
    private Integer id;

    private Integer reportId;

    @Lob
    private String expression;

    private String expressionName;
}
