package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import com.increff.omni.reporting.model.constants.DateType;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Setter
@Getter
@Table(name = "input_control", indexes = {@Index(name = "idx_scope_displayName", columnList = "scope,displayName"),
        @Index(name = "idx_scope_paramName", columnList = "scope,paramName"), @Index(name = "idx_schemaVersionId",
        columnList = "schemaVersionId")})
public class InputControlPojo extends AbstractVersionedPojo{

    @Id
    @TableGenerator(name = "organization", pkColumnValue = "organization", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "organization")
    private Integer id;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String paramName; // Parameter name in Report query

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private InputControlScope scope;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private InputControlType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private DateType dateType;

    @Column(nullable = false)
    private Integer schemaVersionId;

}
