package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "default_value", indexes = {@Index(name = "idx_dashboard_control_id_chart_alias", columnList = "dashboardId, controlId, chartAlias")})
public class DefaultValuePojo extends AbstractVersionedPojo{
    @Id
    @TableGenerator(name = "default_value", pkColumnValue = "default_value")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "default_value")
    private Integer id;
    @Column(nullable = false)
    private Integer dashboardId;
    @Column(nullable = false)
    private Integer controlId;
    @Column(nullable = false)
    private String chartAlias;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String defaultValue;

    public DefaultValuePojo() {
    }

    public DefaultValuePojo(Integer dashboardId, Integer controlId, String chartAlias, String defaultValue) {
        this.dashboardId = dashboardId;
        this.controlId = controlId;
        this.chartAlias = chartAlias;
        this.defaultValue = defaultValue;
    }


}
