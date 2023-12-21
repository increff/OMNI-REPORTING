package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

//import javax.persistence.*;
import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "connection")
public class ConnectionPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "connection", pkColumnValue = "connection" ,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "connection")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String host;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
}
