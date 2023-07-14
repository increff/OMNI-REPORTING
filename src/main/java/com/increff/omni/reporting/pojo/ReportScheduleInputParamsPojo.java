package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_schedule_input_params", uniqueConstraints = {
        @UniqueConstraint(name = "uk_scheduleId_paramKey", columnNames = {"scheduleId", "paramKey"})})
public class ReportScheduleInputParamsPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_schedule_input_params_sequence", pkColumnValue = "report_schedule_input_params_sequence", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_schedule_input_params_sequence")
    private Integer id;

    @Column(nullable = false)
    private Integer scheduleId;

    @Column(nullable = false)
    private String paramKey;

    @Column(columnDefinition = "LONGTEXT")
    private String paramValue;

    @Column(columnDefinition = "LONGTEXT")
    private String displayValue;
}
