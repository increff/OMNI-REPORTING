package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.increff.omni.reporting.helper.PipelineTestHelper.getGCPPipelineConfig;
import static com.increff.omni.reporting.helper.PipelineTestHelper.getPipelineForm;
import static com.increff.omni.reporting.util.ConvertUtil.getJavaObjectFromJson;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class PipelineDtoTest extends AbstractTest {
    
    @Autowired
    private PipelineDto pipelineDto;

    @Test
    public void testAdd() throws ApiException {
        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        PipelineData pipelineData = pipelineDto.add(pipelineForm);
        assertEquals(pipelineForm.getName(), pipelineData.getName());
        assertEquals(pipelineForm.getType(), pipelineData.getType());

        PipelineConfigData configData = getJavaObjectFromJson(pipelineForm.getConfigs().toString(), PipelineConfigData.class);
        assertEquals(configData.getBucketUrl(), pipelineData.getConfigs().getBucketUrl());
        assertEquals(configData.getBucketName(), pipelineData.getConfigs().getBucketName());

    }

    @Test
    public void testUpdate() throws ApiException {
        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        PipelineData pipelineData = pipelineDto.add(pipelineForm);

        pipelineForm.setName("Pipeline 2");
        pipelineDto.update(pipelineData.getId(), pipelineForm);
        PipelineData updatedData = pipelineDto.getPipelineById(pipelineData.getId());
        assertEquals(pipelineForm.getName(), updatedData.getName());
    }

    @Test//getAll
    public void testGetAllPipelines() throws ApiException {
        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        pipelineDto.add(pipelineForm);
        pipelineForm.setName("Pipeline 2");
        pipelineDto.add(pipelineForm);

        List<PipelineData> data = pipelineDto.getPipelinesByUserOrg();
        assertEquals(2, data.size());
    }

    @Test
    public void testGetPipelineById() throws ApiException {
        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        PipelineData pipelineData = pipelineDto.add(pipelineForm);

        PipelineData fetchedData = pipelineDto.getPipelineById(pipelineData.getId());
        assertEquals(pipelineData.getName(), fetchedData.getName());
    }

}