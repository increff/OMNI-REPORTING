package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "reportValidationGroup", indexes = {
        @Index(name = "idx_reportId_groupName_reportControlId", columnList = "reportId, groupName, reportControlId", unique = true)
})
public class ReportValidationGroupPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_validation_group_sequence", pkColumnValue = "report_validation_group_sequence",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_validation_group_sequence")
    private Integer id;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ValidationType type;

    // for now, validation value is used for DATE range, in future it can be a key value pair
    private Integer validationValue = 0;

    @Column(nullable = false)
    private Integer reportId;

    @Column(nullable = false)
    private Boolean isSystemValidation = false;

    @Column(nullable = false)
    private Integer reportControlId;
}
