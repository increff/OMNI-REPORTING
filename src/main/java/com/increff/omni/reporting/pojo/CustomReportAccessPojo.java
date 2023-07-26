package com.increff.omni.reporting.pojo;


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
// TODO: 17/07/23  sftp access(temp), db whitelisting
// TODO: 17/07/23 sequence remove
// TODO: 17/07/23 look for multiple tables with same sequence name
// TODO: 18/07/23 hibernate logs 
// TODO: 18/07/23  
