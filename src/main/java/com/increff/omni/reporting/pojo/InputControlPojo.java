package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control", indexes = {@Index(name = "idx_scope_displayName", columnList = "scope,displayName"),
        @Index(name = "idx_scope_paramName", columnList = "scope,paramName")})
public class InputControlPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "organization", pkColumnValue = "organization", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organization")
    private Integer id;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String paramName; // Parameter name in Report query For e.g. itemId in case of query contains ${itemId}

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputControlScope scope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputControlType type;

}
