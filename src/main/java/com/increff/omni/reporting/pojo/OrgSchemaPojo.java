package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "org_schema", uniqueConstraints =
        {@UniqueConstraint(name = "unq_org_schema", columnNames = "orgId, schemaId")})
public class OrgSchemaPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "org_schema", pkColumnValue = "org_schema", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_schema")
    private Integer id;
    @Column(nullable = false)
    private Integer orgId;
    @Column(nullable = false)
    private Integer schemaId;

}
