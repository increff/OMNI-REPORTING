package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.helper.FlowApiHelper.getReportControlPojo;
import static com.increff.omni.reporting.helper.FlowApiHelper.validateValidationType;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InputControlFlowApi extends AbstractApi {

    @Autowired
    private InputControlApi api;

    @Autowired
    private ReportControlsApi reportControlsApi;

    @Autowired
    private ReportApi reportApi;

    public InputControlPojo add(InputControlPojo pojo, String query, List<String> values,
                                Integer reportId, ValidationType validationType) throws ApiException {

        if (pojo.getScope().equals(InputControlScope.LOCAL)) {
            validateLocalControl(reportId, pojo);
        }

        InputControlQueryPojo queryPojo = getQueryPojo(query);
        List<InputControlValuesPojo> valuesList = getValuesPojo(values);

        pojo = api.add(pojo, queryPojo, valuesList);

        if (pojo.getScope().equals(InputControlScope.LOCAL)) {
            validateValidationType(pojo.getType(), validationType);
            reportControlsApi.add(getReportControlPojo(reportId, pojo.getId(), validationType));
        }
        return pojo;
    }

    private InputControlQueryPojo getQueryPojo(String query) {
        if (StringUtil.isEmpty(query))
            return null;
        InputControlQueryPojo pojo = new InputControlQueryPojo();
        pojo.setQuery(query);
        return pojo;
    }

    private List<InputControlValuesPojo> getValuesPojo(List<String> values) {
        if (CollectionUtils.isEmpty(values))
            return new ArrayList<>();

        return values.stream().map(v -> {
            InputControlValuesPojo pojo = new InputControlValuesPojo();
            pojo.setValue(v);
            return pojo;
        }).collect(Collectors.toList());
    }

    private void validateLocalControl(Integer reportId, InputControlPojo pojo) throws ApiException {
        reportApi.getCheck(reportId);

        // Validating if any other control exists with same display or param name
        List<ReportControlsPojo> existingPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = existingPojos.stream().map(ReportControlsPojo::getControlId)
                .collect(Collectors.toList());

        List<InputControlPojo> controlPojos = api.selectMultiple(controlIds);

        List<InputControlPojo> duplicate = controlPojos.stream()
                .filter(i -> (i.getDisplayName().equals(pojo.getDisplayName()) ||
                        i.getParamName().equals(pojo.getParamName())))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(duplicate))
            throw new ApiException(ApiStatus.BAD_DATA, "Another input control present with same display name" +
                    " or param name");
    }

}
