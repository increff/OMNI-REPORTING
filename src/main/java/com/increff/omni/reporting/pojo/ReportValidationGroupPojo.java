package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "reportValidationGroup")
public class ReportValidationGroupPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_validation_group", pkColumnValue = "report_validation_group", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_validation_group")
    private Integer id;

    private String groupName;

    @Enumerated(value = EnumType.STRING)
    private ValidationType type;

    // Todo for now it is used for DATE range, in future it can be a key value pair
    private Integer validationValue = 0;

    private Integer reportId;

    private Integer reportControlId;
}
