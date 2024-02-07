package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.PipelinePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;


@Repository
@Transactional
public class PipelineDao extends AbstractDao<PipelinePojo> {

    public PipelinePojo getCheck(Integer id) {
        return em().find(PipelinePojo.class, id);
    }

    private static final String SELECT_BY_ORG_ID = "SELECT o FROM PipelinePojo o WHERE o.orgId=:orgId";
    public List<PipelinePojo> getByOrgId(Integer orgId) {
        TypedQuery<PipelinePojo> q = createJpqlQuery(SELECT_BY_ORG_ID);
        q.setParameter("orgId", orgId);
        return selectMultiple(q);
    }

    private static final String SELECT_BY_PIPELINE_IDS = "SELECT o FROM PipelinePojo o WHERE o.id IN :ids";
    public List<PipelinePojo> getByPipelineIds(List<Integer> pipelineIds) {
        if(pipelineIds.isEmpty()) return new ArrayList<>();
        TypedQuery<PipelinePojo> q = createJpqlQuery(SELECT_BY_PIPELINE_IDS);
        q.setParameter("ids", pipelineIds);
        return selectMultiple(q);
    }

}
