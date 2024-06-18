package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "orgMapping", indexes = {@Index(name = "idx_schemaVersionId", columnList = "schemaVersionId")},
    uniqueConstraints = {@UniqueConstraint(columnNames = {"orgId", "schemaVersionId"})})
public class OrgMappingPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "org_schema_version", pkColumnValue = "org_schema_version",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_schema_version")
    private Integer id;
    @Column(nullable = false)
    private Integer orgId;
    @Column(nullable = false)
    private Integer schemaVersionId;
    @Column(nullable = false)
    private Integer connectionId;

}
