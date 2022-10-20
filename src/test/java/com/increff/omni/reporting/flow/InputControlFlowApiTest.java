package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.dao.InputControlValuesDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionPojo;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.*;

public class InputControlFlowApiTest extends AbstractTest {

    @Autowired
    private InputControlFlowApi flowApi;
    @Autowired
    private InputControlApi api;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
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
        InputControlQueryPojo queryPojo = api.selectControlQuery(pojo.getId());
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
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaVersionPojo.getId());
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
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaVersionPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
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
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaVersionPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
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
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(), schemaVersionPojo.getId());
        reportApi.add(pojo);

        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
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

    @Test
    public void testUpdateInputControlWithQuery() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        String query = "select * from oms.oms_orders;";
        flowApi.add(inputControlPojo, query, new ArrayList<>(), null);
        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Client ID 2", "clientId2", InputControlScope.LOCAL, InputControlType.SINGLE_SELECT);
        query = "select * from oms.oms_order;";
        inputControlPojo.setId(id);
        flowApi.update(inputControlPojo, query, new ArrayList<>());
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID 2", pojo.getDisplayName());
        assertEquals("clientId2", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.SINGLE_SELECT, pojo.getType());
        InputControlQueryPojo queryPojo = api.selectControlQuery(pojo.getId());
        assertEquals("select * from oms.oms_order;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
    }

    @Test
    public void testUpdateInputControlWithValues() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        List<String> values = Arrays.asList("LIVE", "PACKED");
        flowApi.add(inputControlPojo, null, values, null);
        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Client ID", "clientId2", InputControlScope.LOCAL, InputControlType.MULTI_SELECT);
        inputControlPojo.setId(id);
        values = Arrays.asList("LIVE", "PACKING");
        flowApi.update(inputControlPojo, null, values);
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID", pojo.getDisplayName());
        assertEquals("clientId2", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        List<InputControlValuesPojo> valuesPojoList = api.selectControlValues(Collections.singletonList(pojo.getId()));
        assertEquals(2, valuesPojoList.size());
        assertEquals("LIVE", valuesPojoList.get(0).getValue());
        assertEquals("PACKING", valuesPojoList.get(1).getValue());
    }

    @Test
    public void testGetValuesFromQuery() throws ApiException {
        ConnectionPojo pojo = getConnectionPojo("127.0.0.1", "Dev DB", username, password);
        connectionApi.add(pojo);
        Map<String, String> values = flowApi.getValuesFromQuery("select version();", pojo);
        assertEquals(0, values.size());
    }
}
