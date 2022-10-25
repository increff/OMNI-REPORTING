package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control_values", uniqueConstraints = @UniqueConstraint(name = "uq_controlId_value"
        , columnNames = {"controlId", "value"}))
public class InputControlValuesPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "organization", pkColumnValue = "organization", allocationSize = 1, initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organization")
    private Integer id;
    @Column(nullable = false)
    private Integer controlId;
    @Column(nullable = false)
    private String value;
}
