package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.ValidationType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_controls")
public class ReportControlsPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_controls", pkColumnValue = "report_controls", allocationSize = 1,initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_controls")
    private Integer id;

    private Integer reportId;

    private Integer controlId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ValidationType validationType;
}
