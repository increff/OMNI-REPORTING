package com.increff.omni.reporting.commons;

import com.nextscm.commons.spring.audit.pojo.AuditPojo;
import com.nextscm.commons.spring.common.ApiException;

import java.util.Date;
import java.util.List;

public interface DaoProvider {

    void save(AuditPojo auditPojo);

    List<AuditPojo> getAuditLogsByTime(Date startTimestamp, Date endTimestamp) throws ApiException;

    List<AuditPojo> selectMultiple(String member, Object value) throws ApiException;

    List<AuditPojo> selectByIdAndType(String objectId, String objectType) throws ApiException;
}
