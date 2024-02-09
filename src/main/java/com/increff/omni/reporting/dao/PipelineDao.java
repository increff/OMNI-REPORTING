package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.PipelinePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

@Repository
@Transactional
public class PipelineDao extends AbstractDao<PipelinePojo> {

    private static final String SELECT_BY_ORG_ID_NAME = "SELECT o FROM PipelinePojo o WHERE o.orgId=:orgId AND o.name=:name";

    public PipelinePojo getByOrgIdName(Integer orgId, String name) {
        TypedQuery<PipelinePojo> q = createJpqlQuery(SELECT_BY_ORG_ID_NAME);
        q.setParameter("orgId", orgId);
        q.setParameter("name", name);
        return selectSingleOrNull(q);
    }

}
