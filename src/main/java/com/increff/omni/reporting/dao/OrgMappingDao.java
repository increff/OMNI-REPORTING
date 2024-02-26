package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

@Repository
@Transactional
public class OrgMappingDao extends AbstractDao<OrgMappingPojo> {

    private final String SELECT_BY_ORG_ID_SCHEMA_VERSION_ID = "select p from OrgMappingPojo p where orgId=:orgId and schemaVersionId=:schemaVersionId";

    public OrgMappingPojo selectByOrgIdSchemaVersionId(Integer orgId, Integer schemaVersionId) {
        TypedQuery<OrgMappingPojo> q = createJpqlQuery(SELECT_BY_ORG_ID_SCHEMA_VERSION_ID);
        q.setParameter("orgId", orgId);
        q.setParameter("schemaVersionId", schemaVersionId);
        return selectSingleOrNull(q);
    }
}
