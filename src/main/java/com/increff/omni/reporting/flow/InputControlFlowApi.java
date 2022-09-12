package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQuery;
import com.increff.omni.reporting.pojo.InputControlValues;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
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
                                Integer reportId) throws ApiException {

        if(pojo.getScope().equals(InputControlScope.LOCAL)){
            validateLocalControl(reportId, pojo);
        }

        InputControlQuery queryPojo = getQueryPojo(query);
        List<InputControlValues> valuesList = getValuesPojo(values);

        pojo = api.add(pojo, queryPojo, valuesList);

        if(pojo.getScope().equals(InputControlScope.LOCAL)){
            reportControlsApi.add(getReportControlPojo(reportId, pojo.getId()));
        }
        return pojo;
    }


    private InputControlQuery getQueryPojo(String query) {
        if(query == null)
            return null;
        InputControlQuery pojo = new InputControlQuery();
        pojo.setQuery(query);
        return pojo;
    }

    private List<InputControlValues> getValuesPojo(List<String> values) {
        if(CollectionUtils.isEmpty(values))
            return new ArrayList<>();

        return values.stream().map(v -> {
            InputControlValues pojo = new InputControlValues();
            pojo.setValue(v);
            return pojo;
        }).collect(Collectors.toList());
    }

    private static ReportControlsPojo getReportControlPojo(Integer reportId, Integer controlId) {
        ReportControlsPojo pojo = new ReportControlsPojo();
        pojo.setReportId(reportId);
        pojo.setControlId(controlId);
        return pojo;
    }

    private void validateLocalControl(Integer reportId, InputControlPojo pojo) throws ApiException {
        reportApi.getCheck(reportId);

        //Validating if any other control exists with same display or param name
        List<ReportControlsPojo> existingPojos = reportControlsApi.getByReportId(reportId);
        List<Integer> controlIds = existingPojos.stream().map(ReportControlsPojo::getControlId)
                .collect(Collectors.toList());

        List<InputControlPojo> controlPojos = api.selectMultiple(controlIds);

        List<InputControlPojo> duplicate = controlPojos.stream()
                .filter(i -> (i.getDisplayName().equals(pojo.getDisplayName()) ||
                        i.getParamName().equals(pojo.getParamName())))
                .collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(duplicate))
            throw new ApiException(ApiStatus.BAD_DATA, "Another input control present with same display name" +
                    " or param name");
    }

}
