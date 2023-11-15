package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "dashboard", indexes = {@Index(name = "idx_dashboard_org_id", columnList = "orgId")})
public class DashboardPojo extends AbstractVersionedPojo {
    @Id
    @TableGenerator(name = "dashboard", pkColumnValue = "dashboard")
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "dashboard")
    private Integer id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer orgId;
}
