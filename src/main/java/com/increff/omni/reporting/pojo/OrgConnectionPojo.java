package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.AppName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "org_connection")
public class OrgConnectionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "org_connection", pkColumnValue = "org_connection", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_connection")
    private Integer id;
    @Column(nullable = false, unique = true)
    private Integer orgId;
    @Column(nullable = false)
    private Integer connectionId;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private AppName appName;
}
