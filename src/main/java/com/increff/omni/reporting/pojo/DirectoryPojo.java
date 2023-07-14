package com.increff.omni.reporting.pojo;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
@Table(name = "directory")
public class DirectoryPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "directory_sequence", pkColumnValue = "directory_sequence", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "directory_sequence")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String directoryName;
    @Column(nullable = false)
    private Integer parentId;

}
