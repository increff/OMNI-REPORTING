package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "schedule_pipeline", uniqueConstraints = {@UniqueConstraint(columnNames = {"scheduleId", "pipelineId"})},
    indexes = {@Index(columnList = "pipelineId")})
public class SchedulePipelinePojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "schedule_pipeline", pkColumnValue = "schedule_pipeline")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schedule_pipeline")
    private Integer id;

    @Column(nullable = false)
    private Integer scheduleId;

    @Column(nullable = false)
    private Integer pipelineId;

    @Column(nullable = false)
    private String folderName;

    public SchedulePipelinePojo(Integer scheduleId, Integer pipelineId, String folderName) {
        this.scheduleId = scheduleId;
        this.pipelineId = pipelineId;
        this.folderName = folderName;
    }
}
