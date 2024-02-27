package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.DBType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "connection", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DBType dbType;
}
