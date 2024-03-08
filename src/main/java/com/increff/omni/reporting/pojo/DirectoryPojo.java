package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "directory")
public class DirectoryPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "directory", pkColumnValue = "directory", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "directory")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String directoryName;
    @Column(nullable = false)
    private Integer parentId;

}
