package com.increff.omni.reporting.pojo;

import com.increff.omni.reporting.model.constants.RowHeight;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "dashboard_chart",indexes = {@Index(name = "idx_dashboard_id_chart_alias", columnList = "dashboardId, chartAlias")})
public class DashboardChartPojo extends AbstractVersionedPojo{
    @Id
    @TableGenerator(name = "dashboard_chart", pkColumnValue = "dashboard_chart")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "dashboard_chart")
    private Integer id;
    @Column(nullable = false)
    private Integer dashboardId;
    @Column(nullable = false)
    private String chartAlias;

    @Column(nullable = false)
    private Integer row;
    @Column(nullable = false)
    private Integer col;
    @Column(nullable = false)
    private Integer colWidth;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RowHeight rowHeight;
}
