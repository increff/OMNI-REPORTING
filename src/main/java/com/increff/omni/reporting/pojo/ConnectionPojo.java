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

    private String name;
    private String url;
    private String username;
    private String password;
}
