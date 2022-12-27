package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ReportRequestData {

    private Integer requestId;
    private Integer reportId;
    private String reportName;
    private String orgName;
    private ReportRequestStatus status;
    private ZonedDateTime requestCreationTime;
    private ZonedDateTime requestUpdatedTime;
    private String failureReason;
    private Double fileSize;
    private Integer noOfRows;
}
