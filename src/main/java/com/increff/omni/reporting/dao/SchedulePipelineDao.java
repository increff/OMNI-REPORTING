package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.SchedulePipelinePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class SchedulePipelineDao extends AbstractDao<SchedulePipelinePojo> {

    public SchedulePipelinePojo getCheck(Integer id) {
        return em().find(SchedulePipelinePojo.class, id);
    }

    public final String SELECT_BY_SCHEDULE_ID = "select p from SchedulePipelinePojo p where p.scheduleId = :scheduleId";
    public List<SchedulePipelinePojo> getByScheduleId(Integer scheduleId) {
        return selectMultiple(createJpqlQuery(SELECT_BY_SCHEDULE_ID)
                .setParameter("scheduleId", scheduleId));
    }

    public final String SELECT_BY_PIPELINE_ID = "select p from SchedulePipelinePojo p where p.pipelineId = :pipelineId";
    public List<SchedulePipelinePojo> getByPipelineId(Integer pipelineId) {
        return selectMultiple(createJpqlQuery(SELECT_BY_PIPELINE_ID)
                .setParameter("pipelineId", pipelineId));
    }

}
