package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "schemaVersion")
public class SchemaPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "schema_version", pkColumnValue = "schema_version", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schema_version")
    private Integer id;
    private String name;

}
