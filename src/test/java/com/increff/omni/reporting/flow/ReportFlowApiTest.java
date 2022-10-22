package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrgSchemaPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportFlowApiTest extends AbstractTest {

    @Autowired
    private ReportFlowApi flowApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private OrgSchemaApi orgSchemaApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private InputControlApi inputControlApi;

    @Test
    public void testAddReport() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        ReportPojo pojo = flowApi.addReport(reportPojo);
        assertNotNull(pojo);
        assertEquals(ReportType.STANDARD, pojo.getType());
        assertEquals("Report 1", pojo.getName());
    }

    @Test(expected = ApiException.class)
    public void testAddSameNameWithSameSchema() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        SchemaVersionPojo schemaVersionPojo1 = getSchemaPojo("9.0.2");
        schemaVersionApi.add(schemaVersionPojo1);
        OrgSchemaVersionPojo pojo = getOrgSchemaPojo(100001, schemaVersionPojo1.getId());
        orgSchemaApi.map(pojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        ReportPojo reportPojo1 = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo1.getId());
        ReportPojo reportPojo2 = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo1.getId());
        flowApi.addReport(reportPojo);
        flowApi.addReport(reportPojo1);
        try {
            flowApi.addReport(reportPojo2);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report already present with same name and schema version", e.getMessage());
            List<ReportPojo> reportPojos = flowApi.getAll(100001);
            assertEquals(1, reportPojos.size());
            assertEquals("Report 1", reportPojos.get(0).getName());
            throw e;
        }
    }

    @Test
    public void testUpdateReport() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.CUSTOM
                , directoryPojo.getId(), schemaVersionPojo.getId());
        flowApi.addReport(reportPojo);
        ReportPojo reportPojo2 = getReportPojo("Report 1 - 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        reportPojo2.setId(reportPojo.getId());
        ReportPojo pojo = flowApi.editReport(reportPojo2);
        assertNotNull(pojo);
        assertEquals(ReportType.STANDARD, pojo.getType());
        assertEquals("Report 1 - 1", pojo.getName());
    }

    @Test(expected = ApiException.class)
    public void testUpdateReportWithDuplicateName() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.CUSTOM
                , directoryPojo.getId(), schemaVersionPojo.getId());
        flowApi.addReport(reportPojo);
        ReportPojo reportPojo1 = getReportPojo("Report 1 - 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        flowApi.addReport(reportPojo1);
        ReportPojo reportPojo2 = getReportPojo("Report 1 - 1", ReportType.CUSTOM
                , directoryPojo.getId(), schemaVersionPojo.getId());
        reportPojo2.setId(reportPojo.getId());
        try {
            flowApi.editReport(reportPojo2);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report already present with same name and schema version", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testUpsertQuery() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        ReportPojo pojo = flowApi.addReport(reportPojo);
        ReportQueryPojo queryPojo = getReportQueryPojo("select version();", pojo.getId());
        ReportQueryPojo queryPojo1 = flowApi.upsertQuery(queryPojo);
        assertEquals("select version();", queryPojo1.getQuery());
        queryPojo.setQuery("select version2();");
        queryPojo1 = flowApi.upsertQuery(queryPojo);
        assertEquals("select version2();", queryPojo1.getQuery());
    }

    @Test
    public void testMapReportToControl() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
    }

    @Test(expected = ApiException.class)
    public void testMapReportToLocalControl() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        try {
            flowApi.mapControlToReport(controlsPojo);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Only Global Control can be mapped to a report", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAddMandatoryValidationGroup() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.MANDATORY, Collections.singletonList(inputControlPojo.getId()));
        flowApi.addValidationGroup(reportPojo.getId(), groupForm);
    }

    @Test(expected = ApiException.class)
    public void testAddMandatoryValidationGroup2Times() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.MANDATORY, Collections.singletonList(inputControlPojo.getId()));
        flowApi.addValidationGroup(reportPojo.getId(), groupForm);
        try {
            flowApi.addValidationGroup(reportPojo.getId(), groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Group name already exist for given report, group name : group1", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAddSingleMandatoryValidationGroup() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.SINGLE_MANDATORY, Collections.singletonList(inputControlPojo.getId()));
        flowApi.addValidationGroup(reportPojo.getId(), groupForm);
    }

    @Test(expected = ApiException.class)
    public void testAddDateValidationGroupWrongControlType() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.DATE_RANGE, Collections.singletonList(inputControlPojo.getId()));
        try {
            flowApi.addValidationGroup(reportPojo.getId(), groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("DATE_RANGE validation can only be applied on DATE input controls", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddDateValidationGroupSizeError() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.DATE);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.DATE_RANGE, Collections.singletonList(inputControlPojo.getId()));
        try {
            flowApi.addValidationGroup(reportPojo.getId(), groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("DATE_RANGE validation type should have exactly 2 DATE input controls", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAddDateValidationGroup() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Start Date", "startDate"
                , InputControlScope.GLOBAL, InputControlType.DATE);
        inputControlApi.add(inputControlPojo, null, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("End Date", "endDate"
                , InputControlScope.GLOBAL, InputControlType.DATE);
        inputControlApi.add(inputControlPojo2, null, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ReportControlsPojo controlsPojo2 = getReportControlsPojo(reportPojo.getId(), inputControlPojo2.getId());
        flowApi.mapControlToReport(controlsPojo2);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.DATE_RANGE, Arrays.asList(inputControlPojo.getId(), inputControlPojo2.getId()));
        flowApi.addValidationGroup(reportPojo.getId(), groupForm);
    }

    @Test
    public void testDeleteReportControl() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Start Date", "startDate"
                , InputControlScope.GLOBAL, InputControlType.DATE);
        inputControlApi.add(inputControlPojo, null, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("End Date", "endDate"
                , InputControlScope.GLOBAL, InputControlType.DATE);
        inputControlApi.add(inputControlPojo2, null, new ArrayList<>());
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , 100001, 100001);
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        flowApi.mapControlToReport(controlsPojo);
        ReportControlsPojo controlsPojo2 = getReportControlsPojo(reportPojo.getId(), inputControlPojo2.getId());
        flowApi.mapControlToReport(controlsPojo2);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.DATE_RANGE, Arrays.asList(inputControlPojo.getId(), inputControlPojo2.getId()));
        flowApi.addValidationGroup(reportPojo.getId(), groupForm);
        flowApi.deleteReportControl(reportPojo.getId(), inputControlPojo2.getId());
    }

}
