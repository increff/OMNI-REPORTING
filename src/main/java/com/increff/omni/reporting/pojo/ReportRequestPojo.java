package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_request", indexes = {
        @Index(name = "idx_status_updatedAt", columnList = "status, updatedAt"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_userId", columnList = "userId"),
        @Index(name = "idx_createdAt", columnList = "createdAt")
})
public class ReportRequestPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "report_request", pkColumnValue = "report_request", allocationSize = 1, initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_request")
    private Integer id;

    @Column(nullable = false)
    private Integer orgId;

    @Column(nullable = false)
    private Integer userId;

    private Integer reportId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportRequestStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportRequestType type;

    private Integer noOfRows;

    private Double fileSize;

    private String url;

    @Column(columnDefinition = "TEXT")
    private String failureReason;
}
