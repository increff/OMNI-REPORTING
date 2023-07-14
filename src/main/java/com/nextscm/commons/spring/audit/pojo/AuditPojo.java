package com.nextscm.commons.spring.audit.pojo;

import com.nextscm.commons.spring.audit.pojo.Constants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(indexes = {@Index(name = "index_object", columnList = "objectId, objectType"), @Index(name = "index_timestamp",
        columnList = "timestamp")})
@Setter
@Getter
public class AuditPojo {
    @Id
    @TableGenerator(name = Constants.SEQ_AUDIT_ID, pkColumnValue = Constants.SEQ_AUDIT_ID,
            initialValue = Constants.SEQ_AUDIT, allocationSize = Constants.S_ALLOC)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = Constants.SEQ_AUDIT_ID)
    private long auditId;

    private String objectId;

    private String objectType;

    private String action;

    private String actor;

    private Date timestamp;

    private String description;
}
