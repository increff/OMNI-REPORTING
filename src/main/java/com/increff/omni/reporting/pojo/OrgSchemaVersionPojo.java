package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "orgSchemaVersion", indexes = {@Index(name = "idx_schemaVersionId", columnList = "schemaVersionId")})
public class OrgSchemaVersionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "org_schema_version", pkColumnValue = "org_schema_version",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_schema_version")
    private Integer id;
    @Column(nullable = false, unique = true)
    private Integer orgId;
    @Column(nullable = false)
    private Integer schemaVersionId;

}
