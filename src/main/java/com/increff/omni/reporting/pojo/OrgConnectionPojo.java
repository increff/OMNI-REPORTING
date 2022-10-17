package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "org_connection", uniqueConstraints =
        {@UniqueConstraint(name = "unq_org", columnNames = {"orgId"})})
public class OrgConnectionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "org_connection", pkColumnValue = "org_connection", allocationSize = 1, initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_connection")
    private Integer id;
    @Column(nullable = false)
    private Integer orgId;
    @Column(nullable = false)
    private Integer connectionId;
}
