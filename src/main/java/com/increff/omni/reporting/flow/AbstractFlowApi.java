package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.validators.DateValidator;
import com.increff.omni.reporting.validators.MandatoryValidator;
import com.increff.omni.reporting.validators.SingleMandatoryValidator;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.increff.account.client.SecurityUtil.getPrincipal;

@Service
public class AbstractFlowApi extends AbstractAuditApi {

    @Autowired
    private SingleMandatoryValidator singleMandatoryValidator;
    @Autowired
    private MandatoryValidator mandatoryValidator;
    @Autowired
    private DateValidator dateValidator;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private OrgMappingApi orgMappingApi;

    protected static int getOrgId() {
        return getPrincipal().getDomainId();
    }
    protected Integer getSchemaVersionId() throws ApiException{
        return orgMappingApi.getCheckByOrgId(getOrgId()).getSchemaVersionId();
    }

    protected void validate(ReportPojo reportPojo, List<ReportInputParamsPojo> reportInputParamsPojoList)
            throws ApiException {
        List<ReportValidationGroupPojo> reportValidationGroupPojoList = reportValidationGroupApi
                .getByReportId(reportPojo.getId());
        Map<String, List<ReportValidationGroupPojo>> groupedByName = reportValidationGroupPojoList.stream()
                .collect(Collectors.groupingBy(ReportValidationGroupPojo::getGroupName));

        // Run through all the validators for this report
        for (Map.Entry<String, List<ReportValidationGroupPojo>> validationList : groupedByName.entrySet()) {
            List<ReportValidationGroupPojo> groupPojoList = validationList.getValue();
            ValidationType type = groupPojoList.get(0).getType();
            List<ReportControlsPojo> reportControlsPojoList = reportControlsApi.getByIds(groupPojoList
                    .stream().map(ReportValidationGroupPojo::getReportControlId).collect(Collectors.toList()));
            List<InputControlPojo> inputControlPojoList = controlApi.selectByIds(reportControlsPojoList.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList()));
            List<String> paramValues = new ArrayList<>();
            List<String> displayValues = new ArrayList<>();

            inputControlPojoList.forEach(i -> {
                ReportInputParamsPojo p = reportInputParamsPojoList.stream().filter(r -> r.getParamKey()
                        .equals(i.getParamName())).collect(Collectors.toList()).get(0);
                paramValues.add(p.getParamValue());
                displayValues.add(i.getDisplayName());
            });
            runValidators(reportPojo, groupPojoList, type, paramValues, displayValues, ReportRequestType.USER);
        }

    }

    protected void runValidators(ReportPojo reportPojo, List<ReportValidationGroupPojo> groupPojoList
            , ValidationType type, List<String> paramValues, List<String> displayValues,
                                 ReportRequestType requestType) throws ApiException {
        switch (type) {
            case SINGLE_MANDATORY:
                singleMandatoryValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue(), requestType);
                break;
            case MANDATORY:
                mandatoryValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue(), requestType);
                break;
            case DATE_RANGE:
                dateValidator.validate(displayValues, paramValues, reportPojo.getName()
                        , groupPojoList.get(0).getValidationValue(), requestType);
                break;
            default:
                throw new ApiException(ApiStatus.BAD_DATA, "Invalid Validation Type");
        }
    }
}
