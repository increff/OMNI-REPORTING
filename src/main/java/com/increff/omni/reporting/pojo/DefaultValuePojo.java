package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class DefaultValuePojo extends AbstractVersionedPojo{
    @Id
    @TableGenerator(name = "default_value", pkColumnValue = "default_value")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "default_value")
    private Integer id;
    @Column(nullable = false)
    private Integer dashboardId;
    @Column(nullable = false)
    private Integer controlId;
    @Column(nullable = false)
    private String defaultValue;
}
