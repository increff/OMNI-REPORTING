package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.PipelineType;
import com.increff.omni.reporting.model.data.PipelineFlowData;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.omni.reporting.pojo.SchedulePipelinePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.PipelineTestHelper.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SchedulePipelineApiTest extends AbstractTest {

    @Autowired
    private SchedulePipelineApi schedulePipelineApi;

    private final Integer scheduleId = 1;
    private final Integer pipelineId = 1;
    private final String folderName = "folderTest";

    @Test
    public void testAdd() throws ApiException {
        SchedulePipelinePojo pojo = getSchedulePipelinePojo(scheduleId, pipelineId, folderName);
        schedulePipelineApi.add(pojo);

        SchedulePipelinePojo existing = schedulePipelineApi.getCheck(pojo.getId());
        assertEquals(pojo, existing);
    }

    @Test
    public void testGetByScheduleId() throws ApiException {
        SchedulePipelinePojo pojo = getSchedulePipelinePojo(scheduleId, pipelineId, folderName);
        schedulePipelineApi.add(pojo);

        List<SchedulePipelinePojo> pojos = schedulePipelineApi.getByScheduleId(scheduleId);
        assertEquals(1, pojos.size());
        assertEquals(pojo, pojos.get(0));
    }

    @Test
    public void testGetByPipelineId() throws ApiException {
        SchedulePipelinePojo pojo = getSchedulePipelinePojo(scheduleId, pipelineId, folderName);
        schedulePipelineApi.add(pojo);

        List<SchedulePipelinePojo> pojos = schedulePipelineApi.getByPipelineId(pipelineId);
        assertEquals(1, pojos.size());
        assertEquals(pojo, pojos.get(0));
    }

    @Test
    public void testDeleteByScheduleId() throws ApiException {
        SchedulePipelinePojo pojo = getSchedulePipelinePojo(scheduleId, pipelineId, folderName);
        schedulePipelineApi.add(pojo);

        List<SchedulePipelinePojo> pojos = schedulePipelineApi.getByPipelineId(pipelineId);
        assertEquals(1, pojos.size());
        assertEquals(pojo, pojos.get(0));

        schedulePipelineApi.deleteByScheduleId(scheduleId);
        pojos = schedulePipelineApi.getByScheduleId(scheduleId);
        assertEquals(0, pojos.size());
    }


    @Test
    public void testUpsert() throws ApiException {
        SchedulePipelinePojo pojo = getSchedulePipelinePojo(scheduleId, pipelineId, folderName);
        schedulePipelineApi.add(pojo);

        List<SchedulePipelinePojo> pojos = schedulePipelineApi.getByPipelineId(pipelineId);
        assertEquals(1, pojos.size());
        assertEquals(pojo, pojos.get(0));

        Integer newPipelineId = 2;
        String newFolderName = "newFolderTest";
        List<PipelineFlowData> pipelineFlowData = Collections.singletonList(getPipelineFlowData(newPipelineId, newFolderName));
        schedulePipelineApi.upsert(scheduleId, pipelineFlowData);

        pojos = schedulePipelineApi.getByScheduleId(scheduleId);
        assertEquals(1, pipelineFlowData.size());
        assertEquals(newPipelineId, pojos.get(0).getPipelineId());
        assertEquals(newFolderName, pojos.get(0).getFolderName());
    }
}