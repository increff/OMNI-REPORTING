package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import com.increff.omni.reporting.pojo.ReportPojo;

public class ReportTestHelper {

    public static ReportPojo getReportPojo(String reportName, ReportType reportType, Integer directoryId, Integer schemaId) {
        ReportPojo pojo = new ReportPojo();
        pojo.setName(reportName);
        pojo.setType(reportType);
        pojo.setDirectoryId(directoryId);
        pojo.setSchemaId(schemaId);
        return pojo;
    }

    public static ReportControlsPojo getReportControlsPojo(Integer reportId, Integer controlId){
        ReportControlsPojo reportControlsPojo = new ReportControlsPojo();
        reportControlsPojo.setReportId(reportId);
        reportControlsPojo.setControlId(controlId);
        return reportControlsPojo;
    }
}
