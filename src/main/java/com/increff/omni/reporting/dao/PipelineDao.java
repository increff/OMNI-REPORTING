package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class PipelineDao extends AbstractDao<PipelinePojo> {

    private static final String SELECT_BY_ORG_ID_NAME = "SELECT o FROM PipelinePojo o WHERE o.orgId=:orgId AND o.name=:name";
    private static final String SELECT_BY_IDS = "SELECT o FROM PipelinePojo o WHERE o.id IN :ids";

    public PipelinePojo getByOrgIdName(Integer orgId, String name) {
        TypedQuery<PipelinePojo> q = createJpqlQuery(SELECT_BY_ORG_ID_NAME);
        q.setParameter("orgId", orgId);
        q.setParameter("name", name);
        return selectSingleOrNull(q);
    }

    public List<PipelinePojo> getByIds(List<Integer> ids) {
        TypedQuery<PipelinePojo> q = createJpqlQuery(SELECT_BY_IDS);
        q.setParameter("ids", ids);
        return q.getResultList();
    }


}
