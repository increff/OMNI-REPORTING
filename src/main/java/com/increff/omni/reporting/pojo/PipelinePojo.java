package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.PipelineType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "pipeline", indexes = {@Index(columnList = "type")},
        uniqueConstraints = {@UniqueConstraint(columnNames = {"orgId", "name"})})
public class PipelinePojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "pipeline", pkColumnValue = "pipeline")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "pipeline")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer orgId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PipelineType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String configs;
}
