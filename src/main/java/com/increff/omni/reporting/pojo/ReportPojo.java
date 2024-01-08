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
}, uniqueConstraints = {@UniqueConstraint(name = "uk_schemaVersionId_alias_isDashboard", columnNames = {
        "schemaVersionId", "alias", "isDashboard"})})
public class ReportPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report", pkColumnValue = "report",initialValue = 100000,
            table = "hibernate_sequences", pkColumnName = "sequence_name", valueColumnName = "next_val")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String alias;

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

    private Integer minFrequencyAllowedSeconds;

    @Column(nullable = false)
    private Boolean isDashboard = false;

}
