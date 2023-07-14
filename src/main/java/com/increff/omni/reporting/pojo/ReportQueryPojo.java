package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_query")
public class ReportQueryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_query_sequence", pkColumnValue = "report_query_sequence",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_query_sequence")
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer reportId;

    @Lob
    @Column(nullable = false)
    private String query;

}
