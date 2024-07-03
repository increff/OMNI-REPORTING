package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionPojo;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.OrgTestHelper.*;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportRequestFlowApiTest extends AbstractTest {

    @Autowired
    private ReportRequestFlowApi flowApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private OrgMappingApi orgMappingApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportFlowApi reportFlowApi;

    private ReportPojo commonSetup() throws ApiException {
        OrganizationPojo orgPojo = getOrgPojo(100001, "increff");
        organizationApi.add(orgPojo);
        ConnectionPojo connectionPojo = getConnectionPojo("127.0.0.1", "Dev DB", username, password);
        connectionApi.add(connectionPojo);
        SchemaVersionPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaPojo);
        orgMappingApi.add(OrgMappingTestHelper.getOrgMappingPojo(orgPojo.getId(), schemaPojo.getId(), connectionPojo.getId()));

        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(pojo);
        InputControlPojo inputControlPojo =
                getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT,
                        schemaPojo.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        InputControlPojo inputControlPojo1 = getInputControlPojo("Start Date", "startDate"
                , InputControlScope.GLOBAL, InputControlType.DATE, schemaPojo.getId());
        inputControlApi.add(inputControlPojo1, null, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("End Date", "endDate"
                , InputControlScope.GLOBAL, InputControlType.DATE, schemaPojo.getId());
        inputControlApi.add(inputControlPojo2, null, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, schemaPojo.getId());
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo1.getId());
        reportFlowApi.mapControlToReport(controlsPojo);
        ReportControlsPojo controlsPojo1 = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        reportFlowApi.mapControlToReport(controlsPojo1);
        ReportControlsPojo controlsPojo2 = getReportControlsPojo(reportPojo.getId(), inputControlPojo2.getId());
        reportFlowApi.mapControlToReport(controlsPojo2);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.DATE_RANGE, Arrays.asList(inputControlPojo1.getId(), inputControlPojo2.getId()));
        reportFlowApi.addValidationGroup(reportPojo.getId(), groupForm);
        ValidationGroupForm groupForm1 = getValidationGroupForm("group2", 0
                , ValidationType.SINGLE_MANDATORY, Collections.singletonList(inputControlPojo.getId()));
        reportFlowApi.addValidationGroup(reportPojo.getId(), groupForm1);
        return reportPojo;
    }

    @Test
    public void testAdd() throws ApiException {
        ReportPojo reportPojo = commonSetup();
        ReportRequestPojo reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        ReportInputParamsPojo reportInputParamsPojo = getReportInputParamsPojo(null, "clientId", "'1100002253'");
        ReportInputParamsPojo reportInputParamsPojo1 = getReportInputParamsPojo(null
                , "startDate", "'2022-05-10T10:00:00.000+05:30'");
        ReportInputParamsPojo reportInputParamsPojo2 = getReportInputParamsPojo(null
                , "endDate", "'2022-05-10T12:00:00.000+05:30'");
        List<ReportInputParamsPojo> reportInputParamsPojoList =
                Arrays.asList(reportInputParamsPojo, reportInputParamsPojo1, reportInputParamsPojo2);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
    }

    @Test
    public void testCheckOpenRequests() throws ApiException {
        ReportPojo reportPojo = commonSetup();
        ReportRequestPojo reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        ReportInputParamsPojo reportInputParamsPojo = getReportInputParamsPojo(null, "clientId", "'1100002253'");
        ReportInputParamsPojo reportInputParamsPojo1 = getReportInputParamsPojo(null
                , "startDate", "'2022-05-10T10:00:00.000+05:30'");
        ReportInputParamsPojo reportInputParamsPojo2 = getReportInputParamsPojo(null
                , "endDate", "'2022-05-10T12:00:00.000+05:30'");
        List<ReportInputParamsPojo> reportInputParamsPojoList =
                Arrays.asList(reportInputParamsPojo, reportInputParamsPojo1, reportInputParamsPojo2);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        reportRequestPojo = getReportRequestPojo(reportPojo.getId(), ReportRequestStatus.NEW
                , 100001, 100001, ReportRequestType.USER);
        try {
            flowApi.requestReport(reportRequestPojo, reportInputParamsPojoList);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Wait for existing reports to get executed", e.getMessage());
        }

    }


}
