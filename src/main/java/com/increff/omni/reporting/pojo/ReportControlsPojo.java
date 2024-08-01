package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "report_controls", uniqueConstraints = @UniqueConstraint(name = "uq_reportId_controlId",
        columnNames = {"reportId", "controlId"}))
public class ReportControlsPojo extends AbstractVersionedPojo{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer reportId;

    @Column(nullable = false)
    private Integer controlId;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
