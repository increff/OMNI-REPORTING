package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Setter
@Getter
@Table(name = "report", indexes = {
        @Index(name = "idx_schemaVersionId_name_isChart", columnList = "schemaVersionId, name, isChart",
                unique = true),
        @Index(name = "idx_schemaVersionId_type", columnList = "schemaVersionId, type"),
        @Index(name = "idx_id_schemaVersionId", columnList = "id, schemaVersionId"),
        @Index(name = "idx_alias", columnList = "alias"),
}, uniqueConstraints = {@UniqueConstraint(name = "uk_schemaVersionId_alias_isChart", columnNames = {
        "schemaVersionId", "alias", "isChart"})})
public class ReportPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report", pkColumnValue = "report",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report")
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
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
    private Boolean isChart = false;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private ChartType chartType = ChartType.REPORT;

    @Override
    public String toString() {
        return "ReportPojo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", type=" + type +
                ", directoryId=" + directoryId +
                ", schemaVersionId=" + schemaVersionId +
                ", isEnabled=" + isEnabled +
                ", canSchedule=" + canSchedule +
                ", minFrequencyAllowedSeconds=" + minFrequencyAllowedSeconds +
                ", isChart=" + isChart +
                ", chartType=" + chartType +
                '}';
    }

}
