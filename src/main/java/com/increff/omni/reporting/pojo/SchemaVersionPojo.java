package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "schemaVersion")
public class SchemaVersionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "schema_version", pkColumnValue = "schema_version", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schema_version")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;

}
