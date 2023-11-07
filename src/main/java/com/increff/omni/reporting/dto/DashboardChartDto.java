package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.model.data.DashboardChartData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.util.ValidateUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j
@Setter
// TODO: Add transactional in every dto
public class DashboardChartDto extends AbstractDto {
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DashboardApi dashboardApi;
    @Autowired
    private DashboardChartApi dashboardChartApi;
    @Autowired
    private DefaultValueApi defaultValueApi;
    @Autowired
    private InputControlDto inputControlDto;


    public List<DashboardChartData> addDashboardChart(List<DashboardChartForm> forms, Integer dashboardId) throws ApiException {
        ValidateUtil.validateDashboardChartForms(forms);
        dashboardApi.getCheck(dashboardId, getOrgId());
        List<DashboardChartPojo> pojos = new ArrayList<>();
        List<Integer> reportIds = new ArrayList<>();

        dashboardChartApi.deleteByDashboardId(dashboardId); // Delete all existing charts

        for(DashboardChartForm form : forms){ // Add all new charts
            ReportPojo report = reportApi.getCheckByAliasAndSchema(form.getChartAlias(), getSchemaVersionId(),true);
            DashboardChartPojo pojo = ConvertUtil.convert(form, DashboardChartPojo.class);
            pojo.setDashboardId(dashboardId);
            pojos.add(dashboardChartApi.addDashboardChart(pojo));

            reportIds.add(report.getId());
        }

        // Delete default values for all input controls not used in any of the updated charts
        defaultValueApi.deleteByDashboardIdAndControlIdNotIn(dashboardId, getInputControlIdsUnion(reportIds));

        return ConvertUtil.convert(pojos, DashboardChartData.class);
    }

    private List<Integer> getInputControlIdsUnion(List<Integer> reportIds) throws ApiException {
        Set<Integer> inputControlIds = new HashSet<>();
        for(Integer reportId : reportIds){
            List<InputControlData> inputControlDatas = inputControlDto.selectForReport(reportId);
            inputControlIds.addAll(inputControlDatas.stream().map(InputControlData::getId).collect(Collectors.toList()));
        }
        return new ArrayList<>(inputControlIds);
    }

}
