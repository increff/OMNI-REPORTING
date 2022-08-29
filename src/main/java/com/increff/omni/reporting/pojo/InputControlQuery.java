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
    @TableGenerator(name = "organization", pkColumnValue = "organization", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organization")
    private Integer id;

    @Lob
    private String query;

}
