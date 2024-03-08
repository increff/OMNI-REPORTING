package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "custom_report_access", uniqueConstraints =
@UniqueConstraint(name = "uq_reportId_orgId", columnNames = {"reportId", "orgId"}),
        indexes = @Index(name = "idx_orgId", columnList = "orgId"))
public class CustomReportAccessPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "custom_report_access", pkColumnValue = "custom_report_access", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "custom_report_access")
    private Integer id;

    @Column(nullable = false)
    private Integer reportId;

    @Column(nullable = false)
    private Integer orgId;
}
