package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportTestHelper {

    public static ReportPojo getReportPojo(String reportName, ReportType reportType, Integer directoryId,
                                           Integer schemaVersionId) {
        ReportPojo pojo = new ReportPojo();
        pojo.setName(reportName);
        pojo.setType(reportType);
        pojo.setDirectoryId(directoryId);
        pojo.setSchemaVersionId(schemaVersionId);
        pojo.setAlias(reportName.replace(" ", "_"));
        return pojo;
    }

    public static ReportControlsPojo getReportControlsPojo(Integer reportId, Integer controlId) {
        ReportControlsPojo reportControlsPojo = new ReportControlsPojo();
        reportControlsPojo.setReportId(reportId);
        reportControlsPojo.setControlId(controlId);
        return reportControlsPojo;
    }

    public static ValidationGroupForm getValidationGroupForm(String groupName, Integer validationValue,
                                                             ValidationType validationType,
                                                             List<Integer> reportControlIds) {
        ValidationGroupForm groupForm = new ValidationGroupForm();
        groupForm.setGroupName(groupName);
        groupForm.setValidationValue(validationValue);
        groupForm.setValidationType(validationType);
        groupForm.setControlIds(reportControlIds);
        return groupForm;
    }

    public static ReportForm getReportForm(String name, ReportType type, Integer directoryId, Integer schemaVersionId,
                                           boolean canSchedule, ChartType chartType) {
        ReportForm form = new ReportForm();
        form.setDirectoryId(directoryId);
        form.setSchemaVersionId(schemaVersionId);
        form.setName(name);
        form.setType(type);
        form.setCanSchedule(canSchedule);
        form.setAlias(name.replace(" ", "_"));
        form.setChartType(chartType);
        return form;
    }

    public static ReportForm getChartForm(String name, ReportType type, Integer directoryId, Integer schemaVersionId,
                                           boolean canSchedule, ChartType chartType, Map<String, String> legends) {
        ReportForm form = new ReportForm();
        form.setDirectoryId(directoryId);
        form.setSchemaVersionId(schemaVersionId);
        form.setName(name);
        form.setType(type);
        form.setCanSchedule(canSchedule);
        form.setAlias(name.replace(" ", "_"));
        form.setChartType(chartType);
        form.setIsChart(true);
        form.setLegends(legends);
        return form;
    }

    public static ReportInputParamsPojo getReportInputParamsPojo(Integer reportRequestId, String paramKey,
                                                                 String paramValue) {
        ReportInputParamsPojo pojo = new ReportInputParamsPojo();
        pojo.setReportRequestId(reportRequestId);
        pojo.setParamKey(paramKey);
        pojo.setParamValue(paramValue);
        return pojo;
    }

    public static ReportQueryPojo getReportQueryPojo(String query, Integer reportId) {
        ReportQueryPojo queryPojo = new ReportQueryPojo();
        queryPojo.setQuery(query);
        queryPojo.setReportId(reportId);
        return queryPojo;
    }

    public static ReportValidationGroupPojo getReportValidationGroupPojo(Integer reportId, String groupName
            , ValidationType type, Integer validationValue, Integer reportControlId) {
        ReportValidationGroupPojo pojo = new ReportValidationGroupPojo();
        pojo.setReportId(reportId);
        pojo.setGroupName(groupName);
        pojo.setType(type);
        pojo.setValidationValue(validationValue);
        pojo.setReportControlId(reportControlId);
        return pojo;
    }

    public static ReportRequestForm getReportRequestForm(Integer reportId, Map<String, List<String>> params,
                                                         String timezone) {
        ReportRequestForm form = new ReportRequestForm();
        form.setReportId(reportId);
        form.setParamMap(params);
        form.setTimezone(timezone);
        return form;
    }

    public static ReportQueryTestForm getQueryTestForm() {
        ReportQueryTestForm queryTestForm = new ReportQueryTestForm();
        queryTestForm.setQuery("select * from table where id = <<replace(<id>)>>;");
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put("id", Arrays.asList("1"));
        queryTestForm.setParamMap(paramMap);
        queryTestForm.setTimezone("Asia/Kolkata");
        return queryTestForm;
    }

    public static ReportRequestPojo getReportRequestPojo(Integer reportId, ReportRequestStatus status
            , Integer orgId, Integer userId, ReportRequestType type) {
        ReportRequestPojo pojo = new ReportRequestPojo();
        pojo.setReportId(reportId);
        pojo.setStatus(status);
        pojo.setOrgId(orgId);
        pojo.setUserId(userId);
        pojo.setType(type);
        return pojo;
    }

    public static ReportQueryForm getReportQueryForm(String query) {
        ReportQueryForm queryForm = new ReportQueryForm();
        queryForm.setQuery(query);
        return queryForm;
    }

    public static FavouriteForm getFavoriteForm(Integer favId) {
        FavouriteForm form = new FavouriteForm();
        form.setFavId(favId);
        return form;
    }
}
