package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report")
public class ReportPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report", pkColumnValue = "report", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report")
    private Integer id;

    private String name;

    @Enumerated(value = EnumType.STRING)
    private ReportType type;

    private Integer directoryId;

    private Integer schemaVersionId;
}
