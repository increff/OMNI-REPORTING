package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control_query")
public class InputControlQueryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "input_control_query", pkColumnValue = "input_control_query", allocationSize = 1,
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "input_control_query")
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer controlId;

    @Lob
    @Column(nullable = false)
    private String query;

}
