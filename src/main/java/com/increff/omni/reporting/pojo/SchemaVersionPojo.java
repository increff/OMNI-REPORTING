package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import com.increff.omni.reporting.model.constants.AppName;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Setter
@Getter
@Table(name = "schemaVersion", indexes = {
        @Index(name = "idx_appName", columnList = "appName"),
})
public class SchemaVersionPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "schema_version", pkColumnValue = "schema_version", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "schema_version")
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private AppName appName;

}
