package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "report_query")
public class ReportQueryPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "report_query", pkColumnValue = "report_query",initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "report_query")
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer reportId;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String query;

}
