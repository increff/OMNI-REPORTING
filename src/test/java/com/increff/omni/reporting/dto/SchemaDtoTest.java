package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.data.SchemaVersionData;
import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
//import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SchemaDtoTest extends AbstractTest {

    @Autowired
    private SchemaDto dto;

    @Test
    public void testAddSchema() throws ApiException {
        SchemaVersionForm form = getSchemaForm("9.0.1");
        dto.add(form);
        SchemaVersionData data = dto.selectAll().get(0);
        assertEquals("9.0.1", data.getName());
    }

    @Test
    public void testAddSchemaValidationFail() throws ApiException {
        SchemaVersionForm form = getSchemaForm(null);
        ApiException exception = assertThrows(ApiException.class, () -> {
            dto.add(form);
        });

        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());
    }

    @Test
    public void testUpdateSchema() throws ApiException {
        SchemaVersionForm form = getSchemaForm("9.0.1");
        dto.add(form);
        SchemaVersionData data = dto.selectAll().get(0);
        form.setName("9.0.2");
        dto.update(data.getId() ,form);
        data = dto.selectAll().get(0);
        assertEquals("9.0.2", data.getName());
    }

    @Test
    public void testUpdateSchemaValidationFail() throws ApiException {
        SchemaVersionForm form = getSchemaForm(null);
        ApiException exception = assertThrows(ApiException.class, () -> {
            dto.update(1, form);
        });

// Verify the expected status and error message of the exception
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Input validation failed", exception.getMessage());
    }

    @Test
    public void testGetAllSchema() throws ApiException {
        SchemaVersionForm form = getSchemaForm("9.0.1");
        dto.add(form);
        form = getSchemaForm("9.0.2");
        dto.add(form);
        List<SchemaVersionData> data = dto.selectAll();
        assertEquals(2, data.size());
        assertEquals("9.0.1", data.get(0).getName());
        assertEquals("9.0.2", data.get(1).getName());
    }

}
