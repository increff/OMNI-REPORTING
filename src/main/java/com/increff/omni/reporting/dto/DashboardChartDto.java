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
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.ChartUtil.DEFAULT_VALUE_COMMON_KEY;

@Service
@Log4j
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

        updateDefaultValues(dashboardId, forms);

        return ConvertUtil.convert(pojos, DashboardChartData.class);
    }

    /**
     * Migrate common default values to chart default values as common filter might no longer be common on updating charts in dashboard
     */
    @Transactional(rollbackFor = ApiException.class)
    private void updateDefaultValues(Integer dashboardId, List<DashboardChartForm> forms) throws ApiException {
        Map<String, Map<Integer, InputControlData>> chartAliasControlMap = new HashMap<>();
        Set<Integer> newCommonControlIds = new HashSet<>();
        Set<Integer> existingCommonControlIds = new HashSet<>();

        for(DashboardChartForm form : forms) {
            ReportPojo report = reportApi.getCheckByAliasAndSchema(form.getChartAlias(), getSchemaVersionId(), true);
            List<InputControlData> inputControlDatas = inputControlDto.selectForReport(report.getId());
            chartAliasControlMap.put(report.getAlias(), inputControlDatas.stream().collect(Collectors.toMap(InputControlData::getId, x -> x)));
        }

        // Delete default values for all input controls not used in any of the updated charts
        defaultValueApi.deleteByDashboardIdAndControlIdNotIn(dashboardId, chartAliasControlMap.values().stream().flatMap(x -> x.keySet().stream()).collect(Collectors.toList()));

        chartAliasControlMap.values().stream().map(Map::keySet).collect(Collectors.toList()).forEach(newCommonControlIds::addAll);
        for(Map<Integer, InputControlData> controlMap : chartAliasControlMap.values()) {
            newCommonControlIds.retainAll(controlMap.keySet()); // Get common input control ids for all charts
        }

        List<DefaultValuePojo> commonDefaultPojos = defaultValueApi.getByDashboardId(dashboardId).stream().filter(x -> x.getChartAlias().equals(DEFAULT_VALUE_COMMON_KEY)).collect(Collectors.toList());
        for(DefaultValuePojo commonDefaultPojo : commonDefaultPojos){ // Delete common default values for input controls not common anymore and migrate to chart level
            if(!newCommonControlIds.contains(commonDefaultPojo.getControlId())) {
                defaultValueApi.deleteByDashboardControlChartAlias(dashboardId, commonDefaultPojo.getControlId(), DEFAULT_VALUE_COMMON_KEY);
                List<String> aliases = chartAliasControlMap.entrySet().stream().filter(x -> x.getValue().containsKey(commonDefaultPojo.getControlId())).map(Map.Entry::getKey).collect(Collectors.toList());
                aliases.forEach(alias -> defaultValueApi.upsert(new DefaultValuePojo(dashboardId, commonDefaultPojo.getControlId(), alias, commonDefaultPojo.getDefaultValue())));
            } else existingCommonControlIds.add(commonDefaultPojo.getControlId());
        }

        newCommonControlIds.removeAll(existingCommonControlIds); // Do nothing if common input control is still common
        for(Integer commonControlId : newCommonControlIds) { // Delete chart level default values for newly created common input controls
            defaultValueApi.deleteByDashboardControlChartAliasNotIn(dashboardId, commonControlId, Collections.singletonList(DEFAULT_VALUE_COMMON_KEY));
            // Note :- Do not add default values for newly created common input controls as there may be separate default values on chart level (V1, V2, V3, etc.) and we won't know which one to use for common
        }
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<DashboardChartData> getDashboardCharts(Integer dashboardId) throws ApiException {
        dashboardApi.getCheck(dashboardId, getOrgId());
        return ConvertUtil.convert(dashboardChartApi.getByDashboardId(dashboardId), DashboardChartData.class);
    }

    @Transactional(rollbackFor = ApiException.class)
    private List<Integer> getInputControlIdsUnion(List<Integer> reportIds) throws ApiException {
        Map<String, Map<Integer, InputControlData>> chartAliasControlMap = new HashMap<>();
        Set<Integer> inputControlIds = new HashSet<>();
        for(Integer reportId : reportIds){
            List<InputControlData> inputControlDatas = inputControlDto.selectForReport(reportId);
            inputControlIds.addAll(inputControlDatas.stream().map(InputControlData::getId).collect(Collectors.toList()));
        }
        return new ArrayList<>(inputControlIds);
    }



}
