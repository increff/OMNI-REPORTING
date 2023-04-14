package com.increff.omni.reporting.pojo;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "custom_report_access", uniqueConstraints =
@UniqueConstraint(name = "uq_reportId_orgId", columnNames = {"reportId", "orgId"}),
        indexes = @Index(name = "idx_orgId", columnList = "orgId"))
public class CustomReportAccessPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "custom_report_access", pkColumnValue = "custom_report_access", allocationSize = 1, initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "custom_report_access")
    private Integer id;

    @Column(nullable = false)
    private Integer reportId;

    @Column(nullable = false)
    private Integer orgId;
}
