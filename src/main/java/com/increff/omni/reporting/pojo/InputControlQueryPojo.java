package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control_query")
public class InputControlQueryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "input_control_query_sequence", pkColumnValue = "input_control_query_sequence",
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "input_control_query_sequence")
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer controlId;

    @Lob
    @Column(nullable = false)
    private String query;

}
