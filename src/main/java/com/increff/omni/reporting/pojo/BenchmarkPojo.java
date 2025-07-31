package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "benchmark", uniqueConstraints = {@UniqueConstraint(name = "uq_org_id_report_id", columnNames = {"orgId", "reportId"})},
        indexes = @Index(name = "idx_org_id_report_id", columnList = "orgId, reportId"))
public class BenchmarkPojo extends AbstractVersionedPojo {
    @Id
    @TableGenerator(name = "benchmark", pkColumnValue = "benchmark")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "benchmark")
    private Integer id;
    @Column(nullable = false)
    private Integer orgId;
    //Format [Full Name (username)]
    @Column(nullable = false)
    private String lastUpdatedBy;
    @Column(nullable = false)
    private Integer reportId;
    @Column(nullable = false)
    private Double value;
}
