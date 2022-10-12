package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InputControlFlowApiTest extends AbstractTest {

    @Autowired
    private InputControlFlowApi flowApi;
    @Autowired
    private InputControlApi api;
    @Autowired
    private ReportControlsApi reportControlsApi;
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
    public void testAddGlobalInputControl() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), null);
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID", pojo.getDisplayName());
        assertEquals("clientId", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        InputControlQueryPojo queryPojo = api.selectControlQueries(Collections.singletonList(pojo.getId())).get(0);
        assertEquals("select * from oms.oms_orders;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
    }

    @Test(expected = ApiException.class)
    public void testAddLocalInputControlWithoutReport() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        String query = "select * from oms.oms_orders;";
        try {
            flowApi.add(inputControlPojo, query, new ArrayList<>(), 1);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No report present with id : 1", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAddLocalInputControlWithoutReportControl() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(pojo);
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), pojo.getId());
    }

    @Test
    public void testAddLocalInputControlWithReportControl() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), pojo.getId());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo2, query, new ArrayList<>(), pojo.getId());
    }

    @Test(expected = ApiException.class)
    public void testAddLocalInputControlWithDuplicateReportControlDisplayName() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), pojo.getId());

        InputControlPojo inputControlPojo2 = getInputControlPojo("Client ID", "warehouseId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        query = "select * from oms.oms_orders;";
        try {
            flowApi.add(inputControlPojo2, query, new ArrayList<>(), pojo.getId());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Another input control present with same display name or param name", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddLocalInputControlWithDuplicateReportControlParamName() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), pojo.getId());

        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        query = "select * from oms.oms_orders;";
        try {
            flowApi.add(inputControlPojo2, query, new ArrayList<>(), pojo.getId());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Another input control present with same display name or param name", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddLocalInputControlWithValidationTypeFailure() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaPojo schemaPojo = getSchemaPojo("9.0.1");
        schemaApi.add(schemaPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), pojo.getId());

        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        query = "select * from oms.oms_orders;";
        try {
            flowApi.add(inputControlPojo2, query, new ArrayList<>(), pojo.getId());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Type TEXT, NUMBER or MULTI_SELECT can have MANDATORY or NON_MANDATORY validation type", e.getMessage());
            throw e;
        }
    }
}
