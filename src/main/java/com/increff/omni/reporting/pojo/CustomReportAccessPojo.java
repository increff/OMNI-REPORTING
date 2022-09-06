package com.increff.omni.reporting.pojo;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "custom_report_access")
public class CustomReportAccessPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "custom_report_access", pkColumnValue = "custom_report_access", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "custom_report_access")
    private Integer id;

    private Integer reportId;

    private Integer orgId;
}
