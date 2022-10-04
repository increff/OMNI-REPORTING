package com.increff.omni.reporting.pojo;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "directory", indexes = {})
public class DirectoryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "directory", pkColumnValue = "directory", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "directory")
    private Integer id;
    private String directoryName;
    private Integer parentId;
    // Todo how to enter roots with parentId null? roots here are Standard Reports/ Custom Reports directory


}
