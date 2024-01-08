package com.increff.omni.reporting.pojo;


import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "directory")
public class DirectoryPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "directory", pkColumnValue = "directory", initialValue = 100000,
            table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "directory")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String directoryName;
    @Column(nullable = false)
    private Integer parentId;

}
