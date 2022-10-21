package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.ReportForm;
import com.increff.omni.reporting.model.form.ReportQueryForm;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;

import java.util.List;

public class ReportTestHelper {

    public static ReportPojo getReportPojo(String reportName, ReportType reportType, Integer directoryId, Integer schemaVersionId) {
        ReportPojo pojo = new ReportPojo();
        pojo.setName(reportName);
        pojo.setType(reportType);
        pojo.setDirectoryId(directoryId);
        pojo.setSchemaVersionId(schemaVersionId);
        return pojo;
    }

    public static ReportControlsPojo getReportControlsPojo(Integer reportId, Integer controlId){
        ReportControlsPojo reportControlsPojo = new ReportControlsPojo();
        reportControlsPojo.setReportId(reportId);
        reportControlsPojo.setControlId(controlId);
        return reportControlsPojo;
    }

    public static ValidationGroupForm getValidationGroupForm(String groupName, Integer validationValue, ValidationType validationType, List<Integer> reportControlIds) {
        ValidationGroupForm groupForm = new ValidationGroupForm();
        groupForm.setGroupName(groupName);
        groupForm.setValidationValue(validationValue);
        groupForm.setValidationType(validationType);
        groupForm.setReportControlIds(reportControlIds);
        return groupForm;
    }

    public static ReportForm getReportForm(String name, ReportType type, Integer directoryId, Integer schemaVersionId) {
        ReportForm form = new ReportForm();
        form.setDirectoryId(directoryId);
        form.setSchemaVersionId(schemaVersionId);
        form.setName(name);
        form.setType(type);
        return form;
    }

    public static ReportInputParamsPojo getReportInputParamsPojo(Integer reportRequestId, String paramKey, String paramValue) {
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

    public static ReportRequestPojo getReportRequestPojo(Integer reportId, ReportRequestStatus status
            , Integer orgId, Integer userId) {
        ReportRequestPojo pojo = new ReportRequestPojo();
        pojo.setReportId(reportId);
        pojo.setStatus(status);
        pojo.setOrgId(orgId);
        pojo.setUserId(userId);
        return pojo;
    }

    public static ReportQueryForm getReportQueryForm(String query) {
        ReportQueryForm queryForm = new ReportQueryForm();
        queryForm.setQuery(query);
        return queryForm;
    }
}
