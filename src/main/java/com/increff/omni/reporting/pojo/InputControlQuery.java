package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control_query", indexes = {})
public class InputControlQuery extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "input_control_query", pkColumnValue = "input_control_query", allocationSize = 1,
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "input_control_query")
    private Integer id;

    private Integer controlId;

    @Lob
    private String query;

}
