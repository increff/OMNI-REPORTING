package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report", indexes = {
        @Index(name = "idx_schemaVersionId_name_isDashboard", columnList = "schemaVersionId, name, isDashboard",
                unique = true),
        @Index(name = "idx_schemaVersionId_type", columnList = "schemaVersionId, type"),
        @Index(name = "idx_id_schemaVersionId", columnList = "id, schemaVersionId")
})
public class ReportPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_sequence", pkColumnValue = "report_sequence",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_sequence")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReportType type;

    @Column(nullable = false)
    private Integer directoryId;

    @Column(nullable = false)
    private Integer schemaVersionId;

    @Column(nullable = false)
    private Boolean isEnabled = true;

    @Column(nullable = false)
    private Boolean canSchedule = false;

    @Column(nullable = false)
    private Boolean isDashboard = false;

}
