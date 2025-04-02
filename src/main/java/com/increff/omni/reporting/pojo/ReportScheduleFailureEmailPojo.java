package com.increff.omni.reporting.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "report_schedule_failure_emails", indexes = {
        @Index(name = "idx_scheduleId", columnList = "scheduleId")})
public class ReportScheduleFailureEmailPojo {
    @Id
    @TableGenerator(name = "report_schedule_failure_emails", pkColumnValue = "report_schedule_failure_emails",
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_schedule_failure_emails")
    private Integer id;

    @Column(nullable = false)
    private Integer scheduleId;

    @Column(nullable = false)
    private String sendTo;
}
