package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.increff.omni.reporting.pojo.ReportInputParamsPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.pojo.ReportQueryPojo;

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
}
