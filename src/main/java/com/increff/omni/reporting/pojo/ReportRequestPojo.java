package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_request")
public class ReportRequestPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_request", pkColumnValue = "report_request", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_request")
    private Integer id;

    private Integer orgId;

    private Integer userId;

    private Integer reportId;

    private ReportRequestStatus status;

    //Maps of Params
}
