package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.InputControlTestHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InputControlApiTest extends AbstractTest {

    @Autowired
    private InputControlApi api;

    @Test
    public void testAddInputControlWithQuery() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;", null);
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
    }

    @Test
    public void testAddInputControlWithValues() throws ApiException {
        InputControlPojo inputControlPojo = getInputControlPojo("Item Status", "itemStatus", InputControlScope.GLOBAL, InputControlType.MULTI_SELECT);
        List<InputControlValuesPojo> valuesPojos = getInputControlValuesPojo(Arrays.asList("LIVE","NEW", "PACKED"), null);
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

}
