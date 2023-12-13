package com.increff.omni.reporting.dto;

import com.increff.commons.queryexecutor.constants.FileFormat;
import com.increff.commons.queryexecutor.constants.RequestStatus;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportRequestType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.ReportScheduleForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.service.encryption.common.CryptoCommon;
import com.increff.service.encryption.form.CryptoForm;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.ReportRequestDto.accessControlledKeys;

public class CommonDtoHelper {

    public final static String TIME_ZONE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static Map<String, String> getInputParamMapFromPojoList(
            List<ReportInputParamsPojo> reportInputParamsPojoList) {
        Map<String, String> inputParamMap = new HashMap<>();
        reportInputParamsPojoList.forEach(r -> inputParamMap.put(r.getParamKey(), r.getParamValue()));
        return inputParamMap;
    }

    public static List<InputControlFilterData> prepareFilters(List<ReportScheduleInputParamsPojo> paramsPojos,
                                                              List<InputControlPojo> controlPojos) {
        List<InputControlFilterData> inputControlFilterData = new ArrayList<>();
        paramsPojos.forEach(p -> {
            Optional<InputControlPojo>
                    controlPojo =
                    controlPojos.stream().filter(c -> c.getParamName().equals(p.getParamKey())).findFirst();
            if (controlPojo.isPresent()) {
                InputControlFilterData filterData = new InputControlFilterData();
                filterData.setParamName(controlPojo.get().getParamName());
                filterData.setDisplayName(controlPojo.get().getDisplayName());
                List<String> values = Objects.isNull(p.getParamValue()) ? new ArrayList<>() :
                        ((controlPojo.get().getType().equals(InputControlType.DATE) || controlPojo.get().getType().equals(InputControlType.DATE_TIME)) ? Collections.singletonList(p.getParamValue()) :
                        Arrays.stream(p.getParamValue().split(
                                        ","))
                                .map(CommonDtoHelper::getValueFromQuotes).collect(Collectors.toList()));
                filterData.setValues(values);
                filterData.setType(controlPojo.get().getType());
                inputControlFilterData.add(filterData);
            }
        });
        return inputControlFilterData;
    }

    public static ReportRequestStatus getStatusMapping(RequestStatus status) {
        switch (status) {
            case COMPLETED:
                return ReportRequestStatus.COMPLETED;
            case FAILED:
                return ReportRequestStatus.FAILED;
            default:
                return ReportRequestStatus.REQUESTED;
        }
    }

    public static ReportRequestData getReportRequestData(ReportRequestPojo pojo, ReportPojo reportPojo,
                                                         List<InputControlPojo> controlPojos,
                                                         List<ReportInputParamsPojo> paramsPojoList,
                                                         OrganizationPojo organizationPojo, Integer sequenceNumber) {
        ReportRequestData data = new ReportRequestData();
        data.setRequestCreationTime(pojo.getCreatedAt());
        data.setRequestUpdatedTime(Objects.isNull(pojo.getRequestCompletionTime()) ? pojo.getUpdatedAt() :
                pojo.getRequestCompletionTime());
        data.setStatus(pojo.getStatus().equals(ReportRequestStatus.REQUESTED) ? ReportRequestStatus.IN_PROGRESS :
                pojo.getStatus());
        data.setType(pojo.getType());
        data.setRequestId(pojo.getId());
        data.setReportId(Objects.nonNull(reportPojo) ? reportPojo.getId() : 0);
        data.setReportName(Objects.nonNull(reportPojo) ? reportPojo.getName() : "NA");
        data.setOrgName(organizationPojo.getName());
        data.setFileSize(pojo.getFileSize());
        data.setNoOfRows(pojo.getNoOfRows());
        data.setSequenceNumber(sequenceNumber);
        data.setFailureReason(pojo.getFailureReason());
        data.setFileFormat(pojo.getFileFormat());
        setFiltersApplied(paramsPojoList, data, controlPojos);
        return data;
    }

    public static Map<Integer, DirectoryPojo> getIdToPojoMap(List<DirectoryPojo> directoryPojoList) {
        Map<Integer, DirectoryPojo> idToPojo = new HashMap<>();
        directoryPojoList.forEach(d -> idToPojo.put(d.getId(), d));
        return idToPojo;
    }

    public static String getDirectoryPath(Integer id,
                                          Map<Integer, DirectoryPojo> idToDirectoryPojoList) {
        DirectoryPojo directoryPojo = idToDirectoryPojoList.get(id);
        String directoryPath = directoryPojo.getDirectoryName();
        Integer parentId = directoryPojo.getParentId();
        while (parentId != 0) {
            DirectoryPojo p = idToDirectoryPojoList.get(parentId);
            directoryPath = p.getDirectoryName().concat("/").concat(directoryPath);
            parentId = p.getParentId();
        }
        return directoryPath;
    }

    public static void setFiltersApplied(List<ReportInputParamsPojo> paramsPojoList,
                                         ReportRequestData data,
                                         List<InputControlPojo> controlPojos) {
        List<InputControlFilterData> filterData = new ArrayList<>();
        for (ReportInputParamsPojo reportInputParamsPojo : paramsPojoList) {
            if (accessControlledKeys.contains(reportInputParamsPojo.getParamKey())
                    || reportInputParamsPojo.getParamKey().equals("orgId"))
                continue;
            if (reportInputParamsPojo.getParamKey().equals("timezone")) {
                data.setTimezone(getValueFromQuotes(reportInputParamsPojo.getParamValue()));
                continue;
            }
            Optional<InputControlPojo> controlPojo =
                    controlPojos.stream().filter(c -> c.getParamName().equals(reportInputParamsPojo.getParamKey()))
                            .findFirst();
            if (controlPojo.isPresent()) {
                InputControlFilterData d = new InputControlFilterData();
                d.setType(controlPojo.get().getType());
                d.setParamName(controlPojo.get().getParamName());
                d.setDisplayName(controlPojo.get().getDisplayName());
                List<String> values = Objects.isNull(reportInputParamsPojo.getDisplayValue()) ? new ArrayList<>() :
                        Arrays.stream(reportInputParamsPojo.getDisplayValue().split(
                                        ","))
                                .map(CommonDtoHelper::getValueFromQuotes).collect(Collectors.toList());
                d.setValues(values);
                filterData.add(d);
            }
        }
        data.setFilters(filterData);
    }

    public static Map<Integer, List<ReportInputParamsPojo>> prepareRequestToParamMap(
            List<ReportInputParamsPojo> allParamsPojo) {
        Map<Integer, List<ReportInputParamsPojo>> requestToParamsPojo = new HashMap<>();
        allParamsPojo.forEach(a -> {
            if (requestToParamsPojo.containsKey(a.getReportRequestId())) {
                List<ReportInputParamsPojo> existingParams = requestToParamsPojo.get(a.getReportRequestId());
                existingParams.add(a);
                requestToParamsPojo.put(a.getReportRequestId(), existingParams);
            } else {
                requestToParamsPojo.put(a.getReportRequestId(), new ArrayList<>(Collections.singletonList(a)));
            }
        });
        return requestToParamsPojo;
    }

    public static Map<Integer, OrganizationPojo> prepareOrgIdToPojo(List<OrganizationPojo> organizationPojoList) {
        Map<Integer, OrganizationPojo> orgToPojo = new HashMap<>();
        organizationPojoList.forEach(a -> orgToPojo.put(a.getId(), a));
        return orgToPojo;
    }

    public static void
    sortBasedOnReportControlMappedTime(List<InputControlData> inputControlDataList,
                                                          List<ReportControlsPojo> reportControlsPojos) {
        inputControlDataList.sort((o1, o2) -> {
            ReportControlsPojo p1 = reportControlsPojos.stream().filter(r -> r.getControlId().equals(o1.getId()))
                    .collect(Collectors.toList()).get(0);
            ReportControlsPojo p2 = reportControlsPojos.stream().filter(r -> r.getControlId().equals(o2.getId()))
                    .collect(Collectors.toList()).get(0);
            return Objects.equals(p1.getId(), p2.getId()) ? 0 : (p1.getCreatedAt().isAfter(p2.getCreatedAt()) ? 1 : -1);
        });
    }

    // Zone Offset/Abbreviation will be populated based on DST(DayLight Saving Time) in case it is applicable for a Zone
    // at current timestamp
    public static TimeZoneData convertToTimeZoneData(String timeZoneId) throws ApiException {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZoneId); // exception will be thrown here in case of incorrect timezoneId
        } catch (Exception e) {
            throw new ApiException(ApiStatus.BAD_DATA, "No timeZone exists for zoneID : " + timeZoneId);
        }
        String offSet = LocalDateTime.now().atZone(zoneId).getOffset()
                .getId().replace("Z", "+00:00");
        DateTimeFormatter zoneFormatter = DateTimeFormatter.ofPattern("zzz"); //pattern for TextStyle.SHORT
        String abbreviation = ZonedDateTime.now(zoneId).format(zoneFormatter);
        TimeZoneData timeZoneData = new TimeZoneData();
        timeZoneData.setZoneId(timeZoneId);
        timeZoneData.setZoneOffset(String.format("%s%s", "UTC", offSet));
        timeZoneData.setZoneAbbreviation(abbreviation);
        return timeZoneData;
    }

    public static ReportControlsPojo getReportControlPojo(Integer reportId, Integer controlId) {
        ReportControlsPojo pojo = new ReportControlsPojo();
        pojo.setReportId(reportId);
        pojo.setControlId(controlId);
        return pojo;
    }

    public static List<OrgSchemaData> getOrgSchemaDataList(List<OrgSchemaVersionPojo> pojos,
                                                           List<SchemaVersionPojo> allPojos) {
        Map<Integer, SchemaVersionPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            SchemaVersionPojo pojo = idToPojoMap.get(p.getSchemaVersionId());
            return getOrgSchemaData(p, pojo);
        }).collect(Collectors.toList());
    }

    public static List<OrgConnectionData> getOrgConnectionDataList(List<OrgConnectionPojo> pojos,
                                                                   List<ConnectionPojo> allPojos) {
        Map<Integer, ConnectionPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            ConnectionPojo pojo = idToPojoMap.get(p.getConnectionId());
            return getOrgConnectionData(p, pojo);
        }).collect(Collectors.toList());
    }

    public static OrgSchemaData getOrgSchemaData(OrgSchemaVersionPojo pojo, SchemaVersionPojo schemaVersionPojo) {
        OrgSchemaData data = new OrgSchemaData();
        data.setOrgId(pojo.getOrgId());
        data.setSchemaVersionId(pojo.getSchemaVersionId());
        data.setSchemaName(schemaVersionPojo.getName());
        return data;
    }

    public static OrgConnectionData getOrgConnectionData(OrgConnectionPojo pojo, ConnectionPojo connectionPojo) {
        OrgConnectionData data = new OrgConnectionData();
        data.setOrgId(pojo.getOrgId());
        data.setConnectionId(pojo.getConnectionId());
        data.setConnectionName(connectionPojo.getName());
        return data;
    }

    public static ReportRequestPojo getReportRequestPojo(ReportRequestForm form, int orgId, int userId) {
        ReportRequestPojo reportRequestPojo = new ReportRequestPojo();
        reportRequestPojo.setReportId(form.getReportId());
        reportRequestPojo.setOrgId(orgId);
        reportRequestPojo.setStatus(ReportRequestStatus.NEW);
        reportRequestPojo.setType(ReportRequestType.USER);
        reportRequestPojo.setUserId(userId);
        reportRequestPojo.setFileFormat(form.getFileFormat());
        return reportRequestPojo;
    }

    public static void updateValidationTypes(List<InputControlData> inputControlDataList
            , List<ReportValidationGroupPojo> validationGroupPojoList, List<ReportControlsPojo> reportControlsPojos) {
        // Generate map of report control id to validation types
        Map<Integer, List<ValidationType>> reportControlToValidationTypeMap = new HashMap<>();
        validationGroupPojoList.forEach(v -> {
            if (reportControlToValidationTypeMap.containsKey(v.getReportControlId())) {
                List<ValidationType> ex = reportControlToValidationTypeMap.get(v.getReportControlId());
                ex.add(v.getType());
                reportControlToValidationTypeMap.put(v.getReportControlId(), ex);
            } else
                reportControlToValidationTypeMap.put(v.getReportControlId(),
                        new ArrayList<>(Collections.singletonList(v.getType())));
        });
        // Generate map of input control id to report control id
        Map<Integer, Integer> inputControlToReportControl = reportControlsPojos.stream().collect(Collectors
                .toMap(ReportControlsPojo::getControlId, ReportControlsPojo::getId));
        inputControlDataList.forEach(d -> {
            List<ValidationType> validationTypeList = reportControlToValidationTypeMap.getOrDefault(
                    inputControlToReportControl.get(d.getId()), new ArrayList<>()
            );
            d.setValidationTypes(validationTypeList);
        });
    }

    public static InputControlPojo getInputControlPojoFromOldControl(Integer newSchemaVersionId, InputControlPojo o) {
        InputControlPojo p = new InputControlPojo();
        p.setDisplayName(o.getDisplayName());
        p.setType(o.getType());
        p.setParamName(o.getParamName());
        p.setScope(o.getScope());
        p.setSchemaVersionId(newSchemaVersionId);
        p.setDateType(o.getDateType());
        return p;
    }

    public static List<ReportInputParamsPojo> getReportInputParamsPojoList(Map<String, String> paramMap
            , String timeZone, Integer orgId, Map<String, String> inputDisplayStringMap) {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojo.setDisplayValue(inputDisplayStringMap.getOrDefault(k, v));
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
        ReportInputParamsPojo timeZoneParam = new ReportInputParamsPojo();
        timeZoneParam.setParamKey("timezone");
        timeZoneParam.setParamValue("'" + timeZone + "'");
        reportInputParamsPojoList.add(timeZoneParam);
        ReportInputParamsPojo orgIdParam = new ReportInputParamsPojo();
        orgIdParam.setParamKey("orgId");
        orgIdParam.setParamValue("'" + orgId.toString() + "'");
        reportInputParamsPojoList.add(orgIdParam);
        return reportInputParamsPojoList;
    }

    public static List<ReportScheduleInputParamsPojo> getReportScheduleInputParamsPojoList(Map<String, String> paramMap
            , String timeZone, Integer orgId, Map<String, String> inputDisplayStringMap) {
        List<ReportScheduleInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportScheduleInputParamsPojo reportInputParamsPojo = new ReportScheduleInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojo.setDisplayValue(inputDisplayStringMap.getOrDefault(k, v));
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
        ReportScheduleInputParamsPojo timeZoneParam = new ReportScheduleInputParamsPojo();
        timeZoneParam.setParamKey("timezone");
        timeZoneParam.setParamValue("'" + timeZone + "'");
        reportInputParamsPojoList.add(timeZoneParam);
        ReportScheduleInputParamsPojo orgIdParam = new ReportScheduleInputParamsPojo();
        orgIdParam.setParamKey("orgId");
        orgIdParam.setParamValue("'" + orgId.toString() + "'");
        reportInputParamsPojoList.add(orgIdParam);
        return reportInputParamsPojoList;
    }

    public static void validate(ReportRequestPojo requestPojo, Integer requestId, ReportPojo reportPojo
            , int userId) throws ApiException {
        if (requestPojo.getType().equals(ReportRequestType.USER) && requestPojo.getUserId() != userId) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Logged in user has not requested the report with id : " + requestId);
        }
        if (!Arrays.asList(ReportRequestStatus.COMPLETED, ReportRequestStatus.FAILED)
                .contains(requestPojo.getStatus())) {
            throw new ApiException(ApiStatus.BAD_DATA,
                    "Report request is still in processing, name : " + reportPojo.getName());
        }
    }

    public static ReportValidationGroupPojo getValidationGroupPojoFromExistingPojo(ReportValidationGroupPojo v) {
        ReportValidationGroupPojo validationGroupPojo = new ReportValidationGroupPojo();
        validationGroupPojo.setValidationValue(v.getValidationValue());
        validationGroupPojo.setGroupName(v.getGroupName());
        validationGroupPojo.setReportId(v.getReportId());
        validationGroupPojo.setReportControlId(v.getReportControlId());
        validationGroupPojo.setType(v.getType());
        validationGroupPojo.setIsSystemValidation(v.getIsSystemValidation());
        return validationGroupPojo;
    }

    public static ReportPojo getReportPojoFromExistingPojo(ReportPojo oldReport) {
        ReportPojo reportPojo = new ReportPojo();
        reportPojo.setIsEnabled(oldReport.getIsEnabled());
        reportPojo.setDirectoryId(oldReport.getDirectoryId());
        reportPojo.setName(oldReport.getName());
        reportPojo.setType(oldReport.getType());
        reportPojo.setCanSchedule(oldReport.getCanSchedule());
        reportPojo.setIsChart(oldReport.getIsChart());
        reportPojo.setAlias(oldReport.getAlias());
        reportPojo.setChartType(oldReport.getChartType());
        reportPojo.setMinFrequencyAllowedSeconds(oldReport.getMinFrequencyAllowedSeconds());
        return reportPojo;
    }

    public static CryptoForm getCryptoForm(String password, Integer userId) {
        CryptoForm cryptoForm = new CryptoForm();
        cryptoForm.setKey("key");
        cryptoForm.setValue(password);
        cryptoForm.setAppName("omni-reporting");
        cryptoForm.setOrgId(userId.toString());
        return cryptoForm;
    }

    public static CryptoCommon convertToCryptoForm(String value) {
        CryptoCommon form = new CryptoCommon();
        form.setKey("key");
        form.setValue(value);
        return form;
    }

    public static Map<Integer, List<ReportRequestPojo>> groupByOrgID(List<ReportRequestPojo> reportRequestPojoList) {
        Map<Integer, List<ReportRequestPojo>> orgToRequests = new HashMap<>();
        for (ReportRequestPojo r : reportRequestPojoList) {
            if (orgToRequests.containsKey(r.getOrgId())) {
                List<ReportRequestPojo> newList = new ArrayList<>(orgToRequests.get(r.getOrgId()));
                newList.add(r);
                orgToRequests.put(r.getOrgId(), newList);
            } else
                orgToRequests.put(r.getOrgId(), Collections.singletonList(r));
        }
        return orgToRequests;
    }

    public static Map<Integer, List<ReportRequestPojo>> groupByUserID(List<ReportRequestPojo> reportRequestPojoList) {
        Map<Integer, List<ReportRequestPojo>> userIdToRequests = new HashMap<>();
        for (ReportRequestPojo r : reportRequestPojoList) {
            if (userIdToRequests.containsKey(r.getUserId())) {
                List<ReportRequestPojo> newList = new ArrayList<>(userIdToRequests.get(r.getUserId()));
                newList.add(r);
                userIdToRequests.put(r.getUserId(), newList);
            } else
                userIdToRequests.put(r.getUserId(), Collections.singletonList(r));
        }
        return userIdToRequests;
    }

    public static ReportRequestPojo convertToReportRequestPojo(ReportSchedulePojo schedulePojo,
                                                               Integer reportId) {
        ReportRequestPojo reportRequestPojo = new ReportRequestPojo();
        reportRequestPojo.setReportId(reportId);
        reportRequestPojo.setType(ReportRequestType.EMAIL);
        reportRequestPojo.setStatus(ReportRequestStatus.NEW);
        reportRequestPojo.setUserId(schedulePojo.getUserId());
        reportRequestPojo.setOrgId(schedulePojo.getOrgId());
        reportRequestPojo.setScheduleId(schedulePojo.getId());
        reportRequestPojo.setFileFormat(FileFormat.CSV);
        return reportRequestPojo;
    }

    public static ReportSchedulePojo convertFormToReportSchedulePojo(ReportScheduleForm form, int orgId, int userId) {
        ReportSchedulePojo schedulePojo = new ReportSchedulePojo();

        String cron = "0" + " " + form.getCronSchedule().getMinute() + " " + form.getCronSchedule().getHour() +
                " " + form.getCronSchedule().getDayOfMonth() + " " + "* " + form.getCronSchedule().getDayOfWeek();

        schedulePojo.setReportAlias(form.getReportAlias());
        schedulePojo.setIsEnabled(form.getIsEnabled());
        schedulePojo.setOrgId(orgId);
        schedulePojo.setUserId(userId);
        schedulePojo.setCron(cron);
        schedulePojo.setNextRuntime(getNextRunTime(cron, form.getTimezone()));
        // New / updated schedule is created with deleted flag false
        schedulePojo.setIsDeleted(false);
        return schedulePojo;
    }

    public static ZonedDateTime getNextRunTime(String cron, String timezone) {
        CronSequenceGenerator generator = new CronSequenceGenerator(cron,
                TimeZone.getTimeZone(ZoneId.of(timezone)));
        Instant instant = generator.next(new Date()).toInstant();
        return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    public static String getValueFromQuotes(String value) {
        if (StringUtil.isEmpty(value))
            return null;
        try {
            if (value.charAt(0) == '\'')
                return value.substring(1, value.length() - 1);
            else
                return value;
        } catch (Exception e) {
            return null;
        }
    }

    public static void validateCronFrequency(ReportPojo reportPojo, ReportSchedulePojo reportSchedulePojo) throws ApiException {
        long cronFrequency = getCronFrequencyInSeconds(reportSchedulePojo.getCron());
        if((Objects.nonNull(reportPojo.getMinFrequencyAllowedSeconds())) &&
                (cronFrequency < reportPojo.getMinFrequencyAllowedSeconds()))
            throw new ApiException(ApiStatus.BAD_DATA, "Cron frequency " + cronFrequency + " is less than minimum allowed frequency "
                    + reportPojo.getMinFrequencyAllowedSeconds() + " seconds. Report name : " + reportPojo.getName() +
                    ", Scheduled Report Alias : " + reportSchedulePojo.getReportAlias() + ", Cron : " + reportSchedulePojo.getCron() + ", Schedule ID : " + reportSchedulePojo.getId());
    }

    public static void validateCronFrequency(ReportPojo reportPojo, List<ReportSchedulePojo> reportSchedulePojos) throws ApiException {
        StringBuilder error = new StringBuilder();
        for(ReportSchedulePojo reportSchedulePojo : reportSchedulePojos) {
            try {
                validateCronFrequency(reportPojo, reportSchedulePojo);
            }
            catch (ApiException e) {
                error.append(e.getMessage()).append("\n");
            }
        }
        if(error.length() > 0)
            throw new ApiException(ApiStatus.BAD_DATA,"Please change existing schedules cron frequency before updating report min frequency.\n" + error.toString());
    }

    public static long getCronFrequencyInSeconds(String cronExpression) throws ApiException {
        CronSequenceGenerator generator = new CronSequenceGenerator(cronExpression);
        long freqIntervalSecondsMin = 1000000000;

        Instant instant = generator.next(new Date()).toInstant();
        ZonedDateTime nextFireTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));

        ZonedDateTime nextToNextFireTime;
        for(int i=0;i<100;i++){ // loop over multiple consecutive fire times to get the minimum frequency as cron expression can be non-periodic
            instant = generator.next(Date.from(nextFireTime.toInstant())).toInstant();
            nextToNextFireTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
            freqIntervalSecondsMin = Math.min(freqIntervalSecondsMin, (nextToNextFireTime.toEpochSecond() - nextFireTime.toEpochSecond()) );
            nextFireTime = nextToNextFireTime;
        }
        return freqIntervalSecondsMin;
    }
}
