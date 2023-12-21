package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

//import javax.persistence.*;
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
    @Column(nullable = false)
    private String query;

}
