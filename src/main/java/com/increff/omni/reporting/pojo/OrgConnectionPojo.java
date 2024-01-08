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
    @TableGenerator(name = "org_connection", pkColumnValue = "org_connection", initialValue = 100000,
            table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "org_connection")
    private Integer id;
    @Column(nullable = false, unique = true)
    private Integer orgId;
    @Column(nullable = false)
    private Integer connectionId;
}
