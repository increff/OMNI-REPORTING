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
import com.nextscm.commons.spring.common.ConvertUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.account.client.SecurityUtil.getPrincipal;

@Log4j
@Service
public class FlowApi extends AbstractAuditApi {

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

    public void validate(ReportPojo reportPojo, List<ReportInputParamsPojo> reportInputParamsPojoList,
                            List<ReportValidationGroupPojo> overridenReportValidationGroupPojoList) throws ApiException {

        List<ReportValidationGroupPojo> reportValidationGroupPojoList = new ArrayList<>();
        if(Objects.nonNull(overridenReportValidationGroupPojoList))
            reportValidationGroupPojoList = overridenReportValidationGroupPojoList;
        else {
            reportValidationGroupPojoList =reportValidationGroupApi
                    .getByReportId(reportPojo.getId());
        }
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


    public List<ReportValidationGroupPojo> mergeValidationGroups(Integer queryReportId, List<ReportPojo> reports) throws ApiException {
        log.debug("Merging validation groups for queryReportId: " + queryReportId + " and reports: " + reports);
        List<ReportValidationGroupPojo> finalGroups = new ArrayList<>();
        List<ReportValidationGroupPojo> allReportValidationGroups = new ArrayList<>();
        Map<Integer, Set<Integer>> reportIdToControlIdsMap = new HashMap<>();
        Map<Integer, List<ReportControlsPojo>> controlIdToReportControlPojosListMap = new HashMap<>();

        for(ReportPojo report : reports){
            allReportValidationGroups.addAll(reportValidationGroupApi.getByReportId(report.getId()));

            List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(report.getId());
            for(ReportControlsPojo reportControlsPojo : reportControlsPojos){
                List<ReportControlsPojo> reportControlPojos = controlIdToReportControlPojosListMap.getOrDefault(reportControlsPojo.getControlId(), new ArrayList<>());
                reportControlPojos.add(reportControlsPojo);
                controlIdToReportControlPojosListMap.put(reportControlsPojo.getControlId(), reportControlPojos);
            }

            List<Integer> controlIds = reportControlsPojos.stream()
                    .map(ReportControlsPojo::getControlId).collect(Collectors.toList());

            reportIdToControlIdsMap.put(report.getId(), new HashSet<>(controlIds));
        }

        // detach allReportValidationGroups from entity to prevent pojo updation on pojo.set
        allReportValidationGroups = allReportValidationGroups.stream().map(g -> ConvertUtil.convert(g, ReportValidationGroupPojo.class))
                .collect(Collectors.toList());
        log.debug("All report validation groups: " + allReportValidationGroups);

        // for every control id in reportIdToControlIdsMap(queryReportId), get all report
        if(reportIdToControlIdsMap.containsKey(queryReportId)) {
            for (Integer queryReportControlId : reportIdToControlIdsMap.get(queryReportId)) {

                // generate validation groups for controls which only exist in queryReportId as we dont care about other validation groups.
                List<Integer> reportControlMappingIds = controlIdToReportControlPojosListMap.get(queryReportControlId).stream()
                        .map(ReportControlsPojo::getId).collect(Collectors.toList());
                List<ReportValidationGroupPojo> controlValidationGroups = allReportValidationGroups.stream()
                        .filter(r -> reportControlMappingIds.contains(r.getReportControlId())).collect(Collectors.toList());

                // get mandatory groups for this control id
                List<ReportValidationGroupPojo> mandatoryGroups = controlValidationGroups.stream()
                        .filter(r -> r.getType().equals(ValidationType.MANDATORY)).collect(Collectors.toList());
                if (!mandatoryGroups.isEmpty()) {
                    ReportValidationGroupPojo group = mandatoryGroups.get(0);
                    group.setReportId(queryReportId);
                    group.setReportControlId(controlIdToReportControlPojosListMap.get(queryReportControlId).stream()
                            .filter(r -> r.getReportId().equals(queryReportId)).collect(Collectors.toList()).get(0).getId());
                    finalGroups.add(group);
                }

                // get date range groups for this control id
                List<ReportValidationGroupPojo> dateRangeGroups = controlValidationGroups.stream()
                        .filter(r -> r.getType().equals(ValidationType.DATE_RANGE)).collect(Collectors.toList());
                if (!dateRangeGroups.isEmpty()) {
                    // get min value of all groups
                    dateRangeGroups.sort(Comparator.comparing(ReportValidationGroupPojo::getValidationValue));

                    ReportValidationGroupPojo group = dateRangeGroups.get(0);
                    group.setReportId(queryReportId);
                    group.setReportControlId(controlIdToReportControlPojosListMap.get(queryReportControlId).stream()
                            .filter(r -> r.getReportId().equals(queryReportId)).collect(Collectors.toList()).get(0).getId());
                    group.setValidationValue(dateRangeGroups.get(0).getValidationValue());
                    finalGroups.add(group);
                }

                // ignore SINGLE_MANDATORY groups as they won't be used for charts/dashboards

            }
        }
        log.debug("Final groups: " + finalGroups);
        return finalGroups;
    }
}
