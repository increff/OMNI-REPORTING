package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.pojo.*;
import com.increff.omni.reporting.util.SqlCmd;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommonDtoHelper {

    public final static String TIME_ZONE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static SqlParams getSqlParams(ConnectionPojo pojo, String query, File file, File errFile,
                                         Integer maxExecutionTime) {
        SqlParams params = new SqlParams();
        params.setPassword(pojo.getPassword());
        params.setUsername(pojo.getUsername());
        params.setHost(pojo.getHost());
        params.setQuery(SqlCmd.massageQuery(query, maxExecutionTime));
        params.setOutFile(file);
        params.setErrFile(errFile);
        return params;
    }

    public static void sortBasedOnReportControlMappedTime(List<InputControlData> inputControlDataList,
                                                          List<ReportControlsPojo> reportControlsPojos) {
        inputControlDataList.sort((o1, o2) -> {
            ReportControlsPojo p1 = reportControlsPojos.stream().filter(r -> r.getControlId().equals(o1.getId()))
                    .collect(Collectors.toList()).get(0);
            ReportControlsPojo p2 = reportControlsPojos.stream().filter(r -> r.getControlId().equals(o2.getId()))
                    .collect(Collectors.toList()).get(0);
            return Objects.equals(p1.getId(), p2.getId()) ? 0 : (p1.getId() > p2.getId() ? 1 : -1);
        });
    }

    public static String massageQuery(String query, Integer maxExecutionTime) {
        return "" //
                + "SET SESSION MAX_EXECUTION_TIME=" + maxExecutionTime * 60 * 1000 + ";\n" //
                + query;
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
        reportRequestPojo.setUserId(userId);
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

    public static SqlParams convert(ConnectionPojo connectionPojo, ReportQueryPojo reportQueryPojo
            , Map<String, String> inputParamsMap, File file, File errorFile, Integer maxExecutionTime) {
        SqlParams sqlParams = new SqlParams();
        sqlParams.setHost(connectionPojo.getHost());
        sqlParams.setUsername(connectionPojo.getUsername());
        sqlParams.setPassword(connectionPojo.getPassword());
        // Replacing query param with input control values
//        String fQuery = SqlCmd.getSubstitutedString(reportQueryPojo.getQuery(), inputParamsMap);
//        sqlParams.setQuery(massageQuery(fQuery, maxExecutionTime));
        sqlParams.setOutFile(file);
        sqlParams.setErrFile(errorFile);
        return sqlParams;
    }

    public static void validate(ReportRequestPojo requestPojo, Integer requestId, ReportPojo reportPojo
            , int userId) throws ApiException {
        if (requestPojo.getUserId() != userId) {
            throw new ApiException(ApiStatus.BAD_DATA, "Logged in user has not requested the report with id : " + requestId);
        }
        if (!Arrays.asList(ReportRequestStatus.COMPLETED, ReportRequestStatus.FAILED).contains(requestPojo.getStatus())) {
            throw new ApiException(ApiStatus.BAD_DATA, "Report request is still in processing, name : " + reportPojo.getName());
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
        reportPojo.setSchemaVersionId(oldReport.getSchemaVersionId());
        return reportPojo;
    }


    public static Map<Integer, List<ReportRequestPojo>> groupByOrgID(List<ReportRequestPojo> reportRequestPojoList) {
        Map<Integer, List<ReportRequestPojo>> orgToRequests = new HashMap<>();
        for (ReportRequestPojo r : reportRequestPojoList) {
            if (orgToRequests.containsKey(r.getOrgId())) {
                List<ReportRequestPojo> newList = orgToRequests.get(r.getOrgId());
                newList.add(r);
                orgToRequests.put(r.getOrgId(), newList);
            }
            else
                orgToRequests.put(r.getOrgId(), Collections.singletonList(r));
        }
        return orgToRequests;
    }
}
