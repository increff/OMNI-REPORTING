package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dao.InputControlQueryDao;
import com.increff.omni.reporting.helper.SchemaTestHelper;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.InputControlTestHelper.*;
import static org.junit.Assert.*;

public class InputControlApiTest extends AbstractTest {

    @Autowired
    private InputControlApi api;
    @Autowired
    private InputControlQueryDao queryDao;
    @Autowired
    private SchemaVersionApi schemaVersionApi;

    SchemaVersionPojo p;
    @Before
    public void initInputControlApi() throws ApiException {
        p = SchemaTestHelper.getSchemaPojo("1.0.0");
        schemaVersionApi.add(p);
    }

    @Test
    public void testAddInputControlWithQuery() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
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

    @Test
    public void testUpdateInputControlWithQuery() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID", pojo.getDisplayName());
        assertEquals("clientId", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        InputControlQueryPojo queryPojo = api.selectControlQuery(pojo.getId());
        assertEquals("select * from oms.oms_orders;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Client ID 2", "clientId", InputControlScope.LOCAL
                , InputControlType.TEXT, p.getId());
        inputControlPojo.setId(id);
        inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_order;", null);
        api.update(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID 2", pojo.getDisplayName());
        assertEquals("clientId", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.TEXT, pojo.getType());
        queryPojo = api.selectControlQuery(pojo.getId());
        assertEquals("select * from oms.oms_order;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
    }

    @Test(expected = ApiException.class)
    public void testUpdateInputControlDuplicateDisplayName() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID", pojo.getDisplayName());
        assertEquals("clientId", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        InputControlQueryPojo queryPojo = api.selectControlQuery(pojo.getId());
        assertEquals("select * from oms.oms_orders;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Client ID 2", "clientId2"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());

        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Client ID 2", "clientId", InputControlScope.LOCAL
                , InputControlType.TEXT, p.getId());
        inputControlPojo.setId(id);
        inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_order;", null);
        try {
            api.update(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Cannot create input control with same display name or param name", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testUpdateInputControlDuplicateParamName() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Client ID", pojo.getDisplayName());
        assertEquals("clientId", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        InputControlQueryPojo queryPojo = api.selectControlQueries(Collections.singletonList(pojo.getId())).get(0);
        assertEquals("select * from oms.oms_orders;", queryPojo.getQuery());
        assertEquals(pojo.getId(), queryPojo.getControlId());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Client ID 2", "clientId2"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());

        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Client ID", "clientId2", InputControlScope.LOCAL
                , InputControlType.TEXT, p.getId());
        inputControlPojo.setId(id);
        inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_order;", null);
        try {
            api.update(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Cannot create input control with same display name or param name", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testUpdateInputControlWithValues() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Item Status", "itemStatus"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        List<InputControlValuesPojo> valuesPojos = getInputControlValuesPojo(Arrays.asList("LIVE", "NEW", "PACKED")
                , null);
        api.add(inputControlPojo, null, valuesPojos);
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Item Status", pojo.getDisplayName());
        assertEquals("itemStatus", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        List<InputControlValuesPojo> valuesPojoList = api.selectControlValues(Collections.singletonList(pojo.getId()));
        assertEquals(3, valuesPojoList.size());
        assertEquals(pojo.getId(), valuesPojoList.get(0).getControlId());
        assertEquals(pojo.getId(), valuesPojoList.get(1).getControlId());
        assertEquals(pojo.getId(), valuesPojoList.get(2).getControlId());
        assertEquals("LIVE", valuesPojoList.get(0).getValue());
        assertEquals("NEW", valuesPojoList.get(1).getValue());
        assertEquals("PACKED", valuesPojoList.get(2).getValue());
        Integer id = inputControlPojo.getId();
        inputControlPojo = getInputControlPojo("Item Status", "itemStatus2"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        inputControlPojo.setId(id);
        valuesPojos = getInputControlValuesPojo(Arrays.asList("PACKING", "PACKED"), null);
        api.update(inputControlPojo, null, valuesPojos);
        pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Item Status", pojo.getDisplayName());
        assertEquals("itemStatus2", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        valuesPojoList = api.selectControlValues(Collections.singletonList(pojo.getId()));
        assertEquals(2, valuesPojoList.size());
        assertEquals(pojo.getId(), valuesPojoList.get(0).getControlId());
        assertEquals(pojo.getId(), valuesPojoList.get(1).getControlId());
        assertEquals("PACKED", valuesPojoList.get(0).getValue());
        assertEquals("PACKING", valuesPojoList.get(1).getValue());
    }

    @Test
    public void testAddInputControlWithValues() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Item Status", "itemStatus"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        List<InputControlValuesPojo> valuesPojos = getInputControlValuesPojo(Arrays.asList("LIVE", "NEW", "PACKED")
                , null);
        api.add(inputControlPojo, null, valuesPojos);
        InputControlPojo pojo = api.getCheck(inputControlPojo.getId());
        assertNotNull(pojo);
        assertEquals("Item Status", pojo.getDisplayName());
        assertEquals("itemStatus", pojo.getParamName());
        assertEquals(InputControlScope.GLOBAL, pojo.getScope());
        assertEquals(InputControlType.MULTI_SELECT, pojo.getType());
        List<InputControlValuesPojo> valuesPojoList = api.selectControlValues(Collections.singletonList(pojo.getId()));
        assertEquals(3, valuesPojoList.size());
        assertEquals(pojo.getId(), valuesPojoList.get(0).getControlId());
        assertEquals(pojo.getId(), valuesPojoList.get(1).getControlId());
        assertEquals(pojo.getId(), valuesPojoList.get(2).getControlId());
        assertEquals("LIVE", valuesPojoList.get(0).getValue());
        assertEquals("NEW", valuesPojoList.get(1).getValue());
        assertEquals("PACKED", valuesPojoList.get(2).getValue());
    }


    @Test
    public void testSelectMultiple() throws ApiException {
        InputControlPojo inputControlPojo1 = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo1 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo1, inputControlQueryPojo1, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());
        List<InputControlPojo> inputControlPojoList = api.selectByIds(Arrays.asList(inputControlPojo1.getId()
                , inputControlPojo2.getId()));
        assertEquals(2, inputControlPojoList.size());
        assertEquals("Client ID", inputControlPojoList.get(0).getDisplayName());
        assertEquals("clientId", inputControlPojoList.get(0).getParamName());
        assertEquals(InputControlScope.GLOBAL, inputControlPojoList.get(0).getScope());
        assertEquals(InputControlType.MULTI_SELECT, inputControlPojoList.get(0).getType());
        assertEquals("Warehouse ID", inputControlPojoList.get(1).getDisplayName());
        assertEquals("warehouseId", inputControlPojoList.get(1).getParamName());
        assertEquals(InputControlScope.GLOBAL, inputControlPojoList.get(1).getScope());
        assertEquals(InputControlType.MULTI_SELECT, inputControlPojoList.get(1).getType());
        List<InputControlPojo> inputControlPojoList1 = api.selectByIds(new ArrayList<>());
        assertEquals(0, inputControlPojoList1.size());
    }

    @Test
    public void testGetByScope() throws ApiException {
        InputControlPojo inputControlPojo1 = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo1 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo1, inputControlQueryPojo1, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());
        InputControlPojo inputControlPojo3 = getInputControlPojo("Channel ID", "channelId"
                , InputControlScope.LOCAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo3 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo3, inputControlQueryPojo3, new ArrayList<>());

        List<InputControlPojo> inputControlPojoList = api.getByScopeAndSchema(InputControlScope.GLOBAL, p.getId());
        assertEquals(2, inputControlPojoList.size());
        assertEquals("Client ID", inputControlPojoList.get(0).getDisplayName());
        assertEquals("clientId", inputControlPojoList.get(0).getParamName());
        assertEquals(InputControlScope.GLOBAL, inputControlPojoList.get(0).getScope());
        assertEquals(InputControlType.MULTI_SELECT, inputControlPojoList.get(0).getType());
        assertEquals("Warehouse ID", inputControlPojoList.get(1).getDisplayName());
        assertEquals("warehouseId", inputControlPojoList.get(1).getParamName());
        assertEquals(InputControlScope.GLOBAL, inputControlPojoList.get(1).getScope());
        assertEquals(InputControlType.MULTI_SELECT, inputControlPojoList.get(1).getType());
    }

    @Test
    public void testGetByScopeAndDisplayName() throws ApiException {
        InputControlPojo inputControlPojo1 = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo1 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo1, inputControlQueryPojo1, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());
        InputControlPojo inputControlPojo3 = getInputControlPojo("Channel ID", "channelId"
                , InputControlScope.LOCAL, InputControlType.SINGLE_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo3 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo3, inputControlQueryPojo3, new ArrayList<>());
        InputControlPojo pojo = api.getByScopeAndDisplayName(InputControlScope.LOCAL, "Channel ID", p.getId());
        assertNotNull(pojo);
        assertEquals("Channel ID", pojo.getDisplayName());
        assertEquals("channelId", pojo.getParamName());
        assertEquals(InputControlScope.LOCAL, pojo.getScope());
        assertEquals(InputControlType.SINGLE_SELECT, pojo.getType());
    }

    @Test
    public void testGetByScopeAndDisplayNameNullObject() throws ApiException {
        InputControlPojo inputControlPojo1 = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo1 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo1, inputControlQueryPojo1, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());
        InputControlPojo inputControlPojo3 = getInputControlPojo("Channel ID", "channelId"
                , InputControlScope.LOCAL, InputControlType.SINGLE_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo3 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo3, inputControlQueryPojo3, new ArrayList<>());
        InputControlPojo pojo = api.getByScopeAndDisplayName(InputControlScope.LOCAL, "Client ID", p.getId());
        assertNull(pojo);
    }

    @Test
    public void testGetByScopeAndParamName() throws ApiException {
        InputControlPojo inputControlPojo1 = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo1 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo1, inputControlQueryPojo1, new ArrayList<>());
        InputControlPojo inputControlPojo2 = getInputControlPojo("Warehouse ID", "warehouseId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo2 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo2, inputControlQueryPojo2, new ArrayList<>());
        InputControlPojo inputControlPojo3 = getInputControlPojo("Channel ID", "channelId"
                , InputControlScope.LOCAL, InputControlType.SINGLE_SELECT, p.getId());
        InputControlQueryPojo inputControlQueryPojo3 = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        api.add(inputControlPojo3, inputControlQueryPojo3, new ArrayList<>());
        InputControlPojo pojo = api.getByScopeAndParamName(InputControlScope.LOCAL, "channelId", p.getId());
        assertNotNull(pojo);
        assertEquals("Channel ID", pojo.getDisplayName());
        assertEquals("channelId", pojo.getParamName());
        assertEquals(InputControlScope.LOCAL, pojo.getScope());
        assertEquals(InputControlType.SINGLE_SELECT, pojo.getType());
    }

}
