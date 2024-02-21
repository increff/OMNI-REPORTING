package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.omni.reporting.pojo.SchedulePipelinePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;


@Repository
@Transactional
public class SchedulePipelineDao extends AbstractDao<SchedulePipelinePojo> {

    private static final String SELECT_BY_SCHEDULE_PIPELINE = "select p from SchedulePipelinePojo p where p.scheduleId=:scheduleId and p.pipelineId=:pipelineId";

    public SchedulePipelinePojo getBySchedulePipeline(Integer scheduleId, Integer pipelineId) {
        TypedQuery<SchedulePipelinePojo> query = createJpqlQuery(SELECT_BY_SCHEDULE_PIPELINE);
        query.setParameter("scheduleId", scheduleId);
        query.setParameter("pipelineId", pipelineId);
        return selectSingleOrNull(query);
    }
}
