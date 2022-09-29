package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "connection", indexes = {})
public class ConnectionPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "connection", pkColumnValue = "connection", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "connection")
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
}
