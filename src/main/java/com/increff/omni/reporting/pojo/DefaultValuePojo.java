package com.increff.omni.reporting.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "default_value", indexes = {@Index(name = "idx_dashboard_id_control_param_name", columnList = "dashboardId, paramName")})
public class DefaultValuePojo extends AbstractVersionedPojo{
    @Id
    @TableGenerator(name = "default_value", pkColumnValue = "default_value")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "default_value")
    private Integer id;
    @Column(nullable = false)
    private Integer dashboardId;
    @Column(nullable = false)
    private String paramName;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String defaultValue;

    public DefaultValuePojo() {
    }

    public DefaultValuePojo(Integer dashboardId, String paramName, String defaultValue) {
        this.dashboardId = dashboardId;
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }


}
