package com.increff.omni.reporting.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.omni.reporting.model.data.ChartLegendsData;
import com.increff.omni.reporting.model.data.PipelineConfigData;
import com.increff.omni.reporting.model.data.PipelineData;
import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
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

    public static PipelineData convertToPipelineData(PipelinePojo pojo) throws ApiException {
        PipelineData data = com.nextscm.commons.spring.common.ConvertUtil.convert(pojo, PipelineData.class);

        data.setConfigs(getJavaObjectFromJson(pojo.getConfigs(), PipelineConfigData.class));

        return data;
    }

    public static List<PipelineData> convertToPipelineData(List<PipelinePojo> pojo) throws ApiException {
        List<PipelineData> datas = new ArrayList<>();
        for (PipelinePojo pipelinePojo : pojo) {
            datas.add(convertToPipelineData(pipelinePojo));
        }
        return datas;
    }

    public static <T> T getJavaObjectFromJson(String credentialsJson, Class<T> fileProviderFormClass) throws ApiException {
        try {
            ObjectMapper obj = new ObjectMapper();
            return obj.readValue(credentialsJson, fileProviderFormClass);
        } catch (Exception e) {
            throw new ApiException(ApiStatus.BAD_DATA, "Error while parsing credentials : " + e.getMessage());
        }
    }


}
