package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConnectionDtoTest extends AbstractTest {

    @Autowired
    private ConnectionDto dto;

    @Test
    public void testAddConnection() throws ApiException {
        ConnectionForm form = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData data = dto.add(form);
        assertNotNull(data);
        assertEquals("dev-db.increff.com", data.getHost());
        assertEquals("Dev DB", data.getName());
        assertEquals("db.user", data.getUsername());
        assertEquals("db.password", data.getPassword());
    }

    @Test
    public void testUpdateConnection() throws ApiException {
        ConnectionForm form = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData data = dto.add(form);
        form = getConnectionForm("dev-db-2.increff.com", "Dev DB 2", "db.user2", "db.password2");
        data = dto.update(data.getId(), form);
        assertNotNull(data);
        assertEquals("dev-db-2.increff.com", data.getHost());
        assertEquals("Dev DB 2", data.getName());
        assertEquals("db.user2", data.getUsername());
        assertEquals("db.password2", data.getPassword());
    }

    @Test
    public void testSelectAll() throws ApiException {
        ConnectionForm form = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        dto.add(form);
        form = getConnectionForm("dev-db-2.increff.com", "Dev DB 2", "db.user2", "db.password2");
        dto.add(form);
        List<ConnectionData> data = dto.selectAll();
        assertEquals(2, data.size());
        assertEquals("dev-db.increff.com", data.get(0).getHost());
        assertEquals("Dev DB", data.get(0).getName());
        assertEquals("db.user", data.get(0).getUsername());
        assertEquals("db.password", data.get(0).getPassword());
        assertEquals("dev-db-2.increff.com", data.get(1).getHost());
        assertEquals("Dev DB 2", data.get(1).getName());
        assertEquals("db.user2", data.get(1).getUsername());
        assertEquals("db.password2", data.get(1).getPassword());

    }
}
