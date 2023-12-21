package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.dto.CommonDtoHelper;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
//import com.nextscm.commons.lang.StringUtil;
import com.increff.omni.reporting.commons.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportScheduleFlowApi extends AbstractFlowApi {

    @Autowired
    private ReportScheduleApi reportScheduleApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private InputControlApi controlApi;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public void add(ReportSchedulePojo pojo, List<String> sendTo,
                    List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojos,
                    ReportPojo reportPojo) throws ApiException {
        CommonDtoHelper.validateCronFrequency(reportPojo, pojo);
        validateGroups(reportPojo, reportScheduleInputParamsPojos);
        reportScheduleApi.add(pojo);
        addEmails(pojo, sendTo);
        reportScheduleApi.addScheduleInputParams(reportScheduleInputParamsPojos, pojo);
    }

    public void edit(ReportSchedulePojo pojo, List<String> sendTo,
                     List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojos,
                     ReportPojo reportPojo) throws ApiException {
        CommonDtoHelper.validateCronFrequency(reportPojo, pojo);
        validateGroups(reportPojo, reportScheduleInputParamsPojos);
        reportScheduleApi.edit(pojo);
        reportScheduleApi.removeExistingEmails(pojo.getId());
        addEmails(pojo, sendTo);
        reportScheduleApi.updateScheduleInputParams(reportScheduleInputParamsPojos, pojo);
    }

    private void validateGroups(ReportPojo reportPojo, List<ReportScheduleInputParamsPojo> reportScheduleInputParamsPojoList)
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
                ReportScheduleInputParamsPojo p = reportScheduleInputParamsPojoList.stream().filter(r -> r.getParamKey()
                        .equals(i.getParamName())).collect(Collectors.toList()).get(0);
                paramValues.add(p.getParamValue());
                displayValues.add(i.getDisplayName());
            });
            runValidators(reportPojo, groupPojoList, type, paramValues, displayValues, ReportRequestType.EMAIL);
        }

    }

    private void addEmails(ReportSchedulePojo pojo, List<String> sendTo) throws ApiException {
        List<ReportScheduleEmailsPojo> emailsPojos = new ArrayList<>();
        sendTo.forEach(e -> {
            if(!StringUtil.isEmpty(e) && VALID_EMAIL_ADDRESS_REGEX.matcher(e).find()) {
                ReportScheduleEmailsPojo emailsPojo = new ReportScheduleEmailsPojo();
                emailsPojo.setSendTo(e);
                emailsPojo.setScheduleId(pojo.getId());
                emailsPojos.add(emailsPojo);
            }
        });
        if(emailsPojos.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "No valid emails given, " + JsonUtil.serialize(sendTo));
        reportScheduleApi.addEmails(emailsPojos);
    }

}
