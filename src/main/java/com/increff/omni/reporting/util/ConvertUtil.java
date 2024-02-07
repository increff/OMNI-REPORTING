package com.increff.omni.reporting.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.increff.omni.reporting.model.data.ChartLegendsData;
import com.increff.omni.reporting.model.data.PipelineData;
import com.increff.omni.reporting.model.form.PipelineForm;
import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import com.increff.omni.reporting.pojo.PipelinePojo;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;

@Log4j
public class ConvertUtil {

    public static ChartLegendsData convertChartLegendsPojoToChartLegendsData(List<ChartLegendsPojo> pojos) {
        ChartLegendsData data = new ChartLegendsData();
        pojos.forEach(pojo -> {
            data.getLegends().put(pojo.getLegendKey(), pojo.getValue());
        });
        return data;
    }

    public static PipelineData convertToPipelineData(PipelinePojo pojo) {
        PipelineData data = com.nextscm.commons.spring.common.ConvertUtil.convert(pojo, PipelineData.class);
        // data.setConfigs(convertStringToJsonNode(pojo.getConfigs())); Do not send configs in response as it contains passwords!!
        data.setConfigs(convertStringToJsonNode("{}"));
        return data;
    }

    public static List<PipelineData> convertToPipelineData(List<PipelinePojo> pojo) {
        List<PipelineData> datas = new ArrayList<>();
        pojo.forEach(p -> {
            datas.add(convertToPipelineData(p));
        });
        return datas;
    }

    public static JsonNode convertStringToJsonNode(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            throw new RuntimeJsonMappingException("Error in converting string to json node" + e.getMessage());
        }
    }


}
