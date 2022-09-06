package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "schema")
public class SchemaPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "schema", pkColumnValue = "schema", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schema")
    private Integer id;
    private String name;

}
