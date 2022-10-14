package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.SqlParams;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.model.data.ReportRequestData;
import com.increff.omni.reporting.model.form.ReportRequestForm;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonDtoHelper {

    public static SqlParams getSqlParams(ConnectionPojo pojo, String query, File file, File errFile) {
        SqlParams params = new SqlParams();
        params.setPassword(pojo.getPassword());
        params.setUsername(pojo.getUsername());
        params.setHost(pojo.getHost());
        params.setQuery(query);
        params.setOutFile(file);
        params.setErrFile(errFile);
        return params;
    }

    public static ReportControlsPojo getReportControlPojo(Integer reportId, Integer controlId) {
        ReportControlsPojo pojo = new ReportControlsPojo();
        pojo.setReportId(reportId);
        pojo.setControlId(controlId);
        return pojo;
    }

    public static List<ReportValidationGroupPojo> getValidationGroupPojoList(ValidationGroupForm groupForm, Integer reportId) {
        List<ReportValidationGroupPojo> groupPojoList = new ArrayList<>();
        groupForm.getReportControlIds().forEach(c -> {
            ReportValidationGroupPojo pojo = new ReportValidationGroupPojo();
            pojo.setGroupName(groupForm.getGroupName());
            pojo.setReportId(reportId);
            pojo.setReportControlId(c);
            pojo.setType(groupForm.getValidationType());
            pojo.setValidationValue(groupForm.getValidationValue());
            groupPojoList.add(pojo);
        });
        return groupPojoList;
    }

    public static List<OrgSchemaData> getOrgSchemaDataList(List<OrgSchemaPojo> pojos, List<SchemaPojo> allPojos) {
        Map<Integer, SchemaPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            SchemaPojo pojo = idToPojoMap.get(p.getSchemaId());
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

    public static OrgSchemaData getOrgSchemaData(OrgSchemaPojo pojo, SchemaPojo schemaPojo) {
        OrgSchemaData data = new OrgSchemaData();
        data.setOrgId(pojo.getOrgId());
        data.setSchemaId(pojo.getSchemaId());
        data.setSchemaName(schemaPojo.getName());
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

    public static List<ReportInputParamsPojo> getReportInputParamsPojoList(Map<String, String> paramMap) {
        List<ReportInputParamsPojo> reportInputParamsPojoList = new ArrayList<>();
        paramMap.forEach((k, v) -> {
            ReportInputParamsPojo reportInputParamsPojo = new ReportInputParamsPojo();
            reportInputParamsPojo.setParamKey(k);
            reportInputParamsPojo.setParamValue(v);
            reportInputParamsPojoList.add(reportInputParamsPojo);
        });
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
}
