package com.increff.omni.reporting.pojo;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "directory")
public class DirectoryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "directory", pkColumnValue = "directory", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "directory")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String directoryName;
    @Column(nullable = false)
    private Integer parentId;

}
