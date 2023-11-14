package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "chart_legends",indexes = {@Index(name = "idx_chart_id", columnList = "chartId")})
// TODO: Add indexes
public class ChartLegendsPojo extends AbstractVersionedPojo {
    @Id
    @TableGenerator(name = "chart_legends", pkColumnValue = "chart_legends")
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "chart_legends")
    private Integer id;
    @Column(nullable = false)
    private Integer chartId; // TODO: Check if this can be converted to chart alias
    @Column(nullable = false)
    private String legendKey;
    @Column(nullable = false)
    private String value;
}
