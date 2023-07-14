package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "org_connection")
public class OrgConnectionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "org_connection_sequence", pkColumnValue = "org_connection_sequence", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_connection_sequence")
    private Integer id;
    @Column(nullable = false, unique = true)
    private Integer orgId;
    @Column(nullable = false)
    private Integer connectionId;
}
