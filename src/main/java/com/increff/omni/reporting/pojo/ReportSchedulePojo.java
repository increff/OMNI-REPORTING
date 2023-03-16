package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportRequestType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Setter
@Getter
@Table(name = "report_schedule", indexes = {@Index(name = "idx_orgId_isEnabled", columnList = "orgId, isEnabled"),
                @Index(name = "idx_isDeleted", columnList = "isDeleted"),
                @Index(name = "idx_nextRuntime_isEnabled", columnList = "nextRuntime, isEnabled")})
public class ReportSchedulePojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_schedule", pkColumnValue = "report_schedule", allocationSize = 1, initialValue =
            100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_schedule")
    private Integer id;

    @Column(nullable = false)
    private Integer orgId;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    private Boolean isEnabled;

    @Column(nullable = false)
    private String cron;

    @Column(nullable = false)
    private ZonedDateTime nextRuntime;

    @Column(nullable = false)
    private Boolean isDeleted;

    private Integer successCount = 0;

    private Integer failureCount = 0;

}
