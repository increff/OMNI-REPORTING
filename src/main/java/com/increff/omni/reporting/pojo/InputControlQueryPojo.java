package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "input_control_query")
public class InputControlQueryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "input_control_query", pkColumnValue = "input_control_query",
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "input_control_query")
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer controlId;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String query;

}
