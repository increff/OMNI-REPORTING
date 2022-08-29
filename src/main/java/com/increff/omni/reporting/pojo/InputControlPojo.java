package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.constants.InputControlScope;
import com.increff.omni.reporting.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control", indexes = {})
public class InputControlPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "organization", pkColumnValue = "organization", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organization")
    private Integer id;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String paramName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputControlScope scope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InputControlType type;

    @Column(nullable = false)
    private Boolean mandatory;

    //Dataset for select
    //Validations ??

}
