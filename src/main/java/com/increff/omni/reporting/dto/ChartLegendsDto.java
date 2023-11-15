package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ChartLegendsApi;
import com.increff.omni.reporting.model.data.ChartLegendsData;
import com.increff.omni.reporting.model.form.ChartLegendsForm;
import com.increff.omni.reporting.pojo.ChartLegendsPojo;
import com.nextscm.commons.spring.common.ApiException;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.increff.omni.reporting.util.ConvertUtil.convertChartLegendsPojoToChartLegendsData;

@Service
@Log4j
@Setter
public class ChartLegendsDto extends AbstractDto {

    @Autowired
    private ChartLegendsApi api;

    @Transactional(rollbackFor = ApiException.class)
    public ChartLegendsData put(Integer chartId, ChartLegendsForm form) throws ApiException {
        checkValid(form);
        List<ChartLegendsPojo> pojos = api.put(chartId, form.getLegends());
        return convertChartLegendsPojoToChartLegendsData(pojos);
    }

}