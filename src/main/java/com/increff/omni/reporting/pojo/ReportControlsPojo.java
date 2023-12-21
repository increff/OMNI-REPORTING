package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

//import javax.persistence.*;
import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_controls", uniqueConstraints = @UniqueConstraint(name = "uq_reportId_controlId",
        columnNames = {"reportId", "controlId"}))
public class ReportControlsPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_controls", pkColumnValue = "report_controls",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_controls")
    private Integer id;

    @Column(nullable = false)
    private Integer reportId;

    @Column(nullable = false)
    private Integer controlId;
}
