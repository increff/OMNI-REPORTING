package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.SchedulePipelineDao;
import com.increff.omni.reporting.model.data.PipelineFlowData;
import com.increff.omni.reporting.pojo.SchedulePipelinePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Log4j
@Transactional(rollbackFor = ApiException.class)
public class SchedulePipelineApi extends AbstractApi {

    @Autowired
    private SchedulePipelineDao dao;

    public SchedulePipelinePojo add(SchedulePipelinePojo pojo) throws ApiException {
        SchedulePipelinePojo existing = dao.getBySchedulePipeline(pojo.getScheduleId(), pojo.getPipelineId());
        if(Objects.nonNull(existing)){
            log.error("Pipeline " + pojo.getPipelineId() + " already exists for schedule " + pojo.getScheduleId());
            throw new ApiException(ApiStatus.BAD_DATA, "Pipeline already exists for schedule");
        }

        dao.persist(pojo);
        return pojo;
    }

    public List<SchedulePipelinePojo> upsert(Integer scheduleId, List<PipelineFlowData> pipelineFlowData) throws ApiException {
        deleteByScheduleId(scheduleId);

        List<SchedulePipelinePojo> newPojos = new ArrayList<>();
        for(PipelineFlowData data : pipelineFlowData) {
            newPojos.add(add(new SchedulePipelinePojo(scheduleId, data.getPipelineId(), data.getFolderName())));
        }
        return newPojos;
    }
    
    private SchedulePipelinePojo getCheck(Integer id) throws ApiException {
        SchedulePipelinePojo pojo = dao.select(id);
        checkNotNull(pojo, "SchedulePipeline does not exist id: " + id);
        return pojo;
    }

    public List<SchedulePipelinePojo> getByScheduleId(Integer scheduleId) {
        return dao.selectMultiple("scheduleId", scheduleId);
    }

    public List<SchedulePipelinePojo> getByPipelineId(Integer pipelineId) {
        return dao.selectMultiple("pipelineId", pipelineId);
    }

    public void deleteByScheduleId(Integer scheduleId) {
        List<SchedulePipelinePojo> pojos = getByScheduleId(scheduleId);
        pojos.forEach(p -> dao.remove(p));
        dao.flush();
    }
}
