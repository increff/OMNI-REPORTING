package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.form.SqlParams;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.data.TimeZoneData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.apache.commons.text.StringSubstitutor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CommonDtoHelper {

    public final static String TIME_ZONE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSxxx";

    public static SqlParams getSqlParams(ConnectionPojo pojo, String query, File file, File errFile, Integer maxExecutionTime) {
        SqlParams params = new SqlParams();
        params.setPassword(pojo.getPassword());
        params.setUsername(pojo.getUsername());
        params.setHost(pojo.getHost());
        params.setQuery(massageQuery(query, maxExecutionTime));
        params.setOutFile(file);
        params.setErrFile(errFile);
        return params;
    }

    public static String massageQuery(String query, Integer maxExecutionTime) {
        return "" //
                + "SET SESSION MAX_EXECUTION_TIME=" + maxExecutionTime * 60 * 1000 + ";\n" //
                + query;
    }

    //Zone Offset/Abbreviation will be populated based on DST(DayLight Saving Time) in case it is applicable for a Zone at current timestamp
    public static TimeZoneData convertToTimeZoneData(String timeZoneId) throws ApiException {
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(timeZoneId); //exception will be thrown here in case of incorrect timezoneId
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

    public static List<OrgSchemaData> getOrgSchemaDataList(List<OrgSchemaVersionPojo> pojos, List<SchemaVersionPojo> allPojos) {
        Map<Integer, SchemaVersionPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            SchemaVersionPojo pojo = idToPojoMap.get(p.getSchemaVersionId());
            return getOrgSchemaData(p, pojo);
        }).collect(Collectors.toList());
    }

    public static List<OrgConnectionData> getOrgConnectionDataList(List<OrgConnectionPojo> pojos, List<ConnectionPojo> allPojos) {
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

    public static List<ReportInputParamsPojo> getReportInputParamsPojoList(Map<String, String> paramMap, String timeZone) {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
        ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
        reportInputParamsPojo.setParamKey("timezone");
        reportInputParamsPojo.setParamValue(timeZone);
        reportInputParamsPojoList.add(reportInputParamsPojo);
        return reportInputParamsPojoList;
    }

    public static ReportRequestData getReportRequestData(ReportRequestPojo pojo, ReportPojo reportPojo) {
        ReportRequestData data = new ReportRequestData();
        data.setRequestCreationTime(pojo.getCreatedAt());
        data.setRequestUpdatedTime(pojo.getUpdatedAt());
        data.setStatus(pojo.getStatus());
        data.setRequestId(pojo.getId());
        data.setReportId(reportPojo.getId());
        data.setReportName(reportPojo.getName());
        return data;
    }

    public static SqlParams convert(ConnectionPojo connectionPojo, ReportQueryPojo reportQueryPojo, Map<String, String> inputParamsMap, File file, File errorFile, Integer maxExecutionTime) {
        SqlParams sqlParams = new SqlParams();
        sqlParams.setHost(connectionPojo.getHost());
        sqlParams.setUsername(connectionPojo.getUsername());
        sqlParams.setPassword(connectionPojo.getPassword());
        // Replacing query param with input control values
        String fQuery = StringSubstitutor.replace(reportQueryPojo.getQuery(), inputParamsMap);
        sqlParams.setQuery(massageQuery(fQuery, maxExecutionTime));
        sqlParams.setOutFile(file);
        sqlParams.setErrFile(errorFile);
        return sqlParams;
    }

    public static Map<Integer, List<ReportRequestPojo>> groupByOrgID(List<ReportRequestPojo> reportRequestPojoList) {
        Map<Integer, List<ReportRequestPojo>> orgToRequests = new HashMap<>();
        reportRequestPojoList.forEach(r -> {
            if(orgToRequests.containsKey(r.getOrgId()))
                orgToRequests.get(r.getOrgId()).add(r);
            else
                orgToRequests.put(r.getOrgId(), Collections.singletonList(r));
        });
        return orgToRequests;
    }
}
