package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportControlsPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.*;

public class ReportControlsApiTest extends AbstractTest {

    @Autowired
    private ReportControlsApi api;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private SchemaApi schemaApi;
    @Autowired
    private ApplicationProperties properties;

    @Test
    public void testAddReportControlPojo() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo reportPojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(ValidationType.MANDATORY, reportPojo.getId(), inputControlPojo.getId());
        api.add(controlsPojo);
        ReportControlsPojo pojo1 = api.select(reportPojo.getId(), inputControlPojo.getId());
        assertNotNull(pojo1);
        assertEquals(reportPojo.getId(), pojo1.getReportId());
        assertEquals(inputControlPojo.getId(), pojo1.getControlId());
        assertEquals(ValidationType.MANDATORY, pojo1.getValidationType());
        ReportControlsPojo pojo2 = api.select(reportPojo.getId(), inputControlPojo.getId() + 1);
        assertNull(pojo2);
    }

    @Test
    public void testUpdateReportControlPojo() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo reportPojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(reportPojo);
        ReportControlsPojo controlsPojo = getReportControlsPojo(ValidationType.MANDATORY, reportPojo.getId(), inputControlPojo.getId());
        api.add(controlsPojo);
        controlsPojo = getReportControlsPojo(ValidationType.NON_MANDATORY, reportPojo.getId(), inputControlPojo.getId());
        api.add(controlsPojo);
        ReportControlsPojo pojo1 = api.select(reportPojo.getId(), inputControlPojo.getId());
        assertNotNull(pojo1);
        assertEquals(reportPojo.getId(), pojo1.getReportId());
        assertEquals(inputControlPojo.getId(), pojo1.getControlId());
        assertEquals(ValidationType.NON_MANDATORY, pojo1.getValidationType());
    }
}
