package com.increff.omni.reporting.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.PipelineType;
import com.increff.omni.reporting.model.data.PipelineFlowData;
import com.increff.omni.reporting.model.form.CronScheduleForm;
import com.increff.omni.reporting.model.form.PipelineDetailsForm;
import com.increff.omni.reporting.model.form.PipelineForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

public class PipelineTestHelper {

    public static PipelineForm getPipelineForm(String name, PipelineType type, JsonNode configs) {
        PipelineForm form = new PipelineForm();
        form.setName(name);
        form.setType(type);
        form.setConfigs(configs);
        return form;
    }

    public static PipelinePojo getPipelinePojo(String name, PipelineType type, String configs, Integer orgId) {
        PipelinePojo pojo = new PipelinePojo();
        pojo.setName(name);
        pojo.setType(type);
        pojo.setConfigs(configs);
        pojo.setOrgId(orgId);
        return pojo;
    }

    public static SchedulePipelinePojo getSchedulePipelinePojo(Integer scheduleId, Integer pipelineId, String folderName) {
        SchedulePipelinePojo pojo = new SchedulePipelinePojo();
        pojo.setScheduleId(scheduleId);
        pojo.setPipelineId(pipelineId);
        pojo.setFolderName(folderName);
        return pojo;
    }

    public static PipelineFlowData getPipelineFlowData(Integer pipelineId, String folderName) {
        PipelineFlowData data = new PipelineFlowData();
        data.setPipelineId(pipelineId);
        data.setFolderName(folderName);
        return data;
    }

    public static JsonNode getGCPPipelineConfig(String bucketUrl, String bucketName, String credentialsJson) throws ApiException {
        return getJsonNode(getGCPConfigString(bucketUrl, bucketName, credentialsJson));
    }


    public static String getGCPConfigString(String bucketUrl, String bucketName, String credentialsJson) {
        return  "{\n" +
                "  \"bucketUrl\": \"" + bucketUrl + "\",\n" +
                "  \"bucketName\": \"" + bucketName + "\",\n" +
                "  \"credentialsJson\": \"" + credentialsJson + "\"\n" +
                "}";
    }

    public static String getAWSConfigString(String bucketUrl, String bucketName, String region, String accessKey, String secretKey) {
        return "{\n" +
                "  \"bucketUrl\": \"" + bucketUrl + "\",\n" +
                "  \"bucketName\": \"" + bucketName + "\",\n" +
                "  \"region\": \"" + region + "\",\n" +
                "  \"accessKey\": \"" + accessKey + "\",\n" +
                "  \"secretKey\": \"" + secretKey + "\"\n" +
                "}";
    }

    public static JsonNode getJsonNode(String json) throws ApiException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error while parsing credentials : " + e.getMessage());
        }
    }

    public static PipelineDetailsForm getPipelineDetailsForm(Integer pipelineId, String folderName) {
        PipelineDetailsForm form = new PipelineDetailsForm();
        form.setPipelineId(pipelineId);
        form.setFolderName(folderName);
        return form;
    }

}
