package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_schedule_emails", indexes = {
        @Index(name = "idx_scheduleId", columnList = "scheduleId")})
public class ReportScheduleEmailsPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_schedule_emails_sequence", pkColumnValue = "report_schedule_emails_sequence",
            initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_schedule_emails_sequence")
    private Integer id;

    @Column(nullable = false)
    private Integer scheduleId;

    @Column(nullable = false)
    private String sendTo;
}
