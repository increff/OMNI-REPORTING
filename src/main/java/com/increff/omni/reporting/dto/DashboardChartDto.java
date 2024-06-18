package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.model.data.DashboardChartData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DashboardChartForm;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.util.ValidateUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.ChartUtil.DEFAULT_VALUE_COMMON_KEY;

@Service
@Log4j2
@Setter
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


    @Transactional(rollbackFor = ApiException.class)
    public List<DashboardChartData> addDashboardChart(List<DashboardChartForm> forms, Integer dashboardId) throws ApiException {
        ValidateUtil.validateDashboardChartForms(forms);
        dashboardApi.getCheck(dashboardId, getOrgId());
        List<DashboardChartPojo> pojos = new ArrayList<>();

        dashboardChartApi.deleteByDashboardId(dashboardId); // Delete all existing charts

        for(DashboardChartForm form : forms){ // Add all new charts
            DashboardChartPojo pojo = ConvertUtil.convert(form, DashboardChartPojo.class);
            pojo.setDashboardId(dashboardId);
            pojos.add(dashboardChartApi.addDashboardChart(pojo));
        }

        updateDefaultValues(dashboardId, forms); // Delete default values for input controls not used in any of the updated charts

        return ConvertUtil.convert(pojos, DashboardChartData.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    private void updateDefaultValues(Integer dashboardId, List<DashboardChartForm> forms) throws ApiException {
        defaultValueApi.deleteByDashboardIdAndControlParamNameNotIn(dashboardId,
                getInputControlParamNamesUnion(forms.stream().map(DashboardChartForm::getChartAlias).collect(Collectors.toList())));
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<DashboardChartData> getDashboardCharts(Integer dashboardId) throws ApiException {
        dashboardApi.getCheck(dashboardId, getOrgId());
        return ConvertUtil.convert(dashboardChartApi.getByDashboardId(dashboardId), DashboardChartData.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    private List<String> getInputControlParamNamesUnion(List<String> chartAliases) throws ApiException {
        Map<String, Map<Integer, InputControlData>> chartAliasControlMap = new HashMap<>();
        Set<String> inputControlIds = new HashSet<>();
        for(String alias : chartAliases){
            ReportPojo report = reportApi.getCheckByAliasAndSchema(alias, getSchemaVersionIds(), true);
            List<InputControlData> inputControlDatas = inputControlDto.selectForReport(report.getId());
            inputControlIds.addAll(inputControlDatas.stream().map(InputControlData::getParamName).collect(Collectors.toList()));
        }
        return new ArrayList<>(inputControlIds);
    }



}
