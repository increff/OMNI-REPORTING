package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.data.DirectoryData;
import com.increff.omni.reporting.model.form.DirectoryForm;
import com.nextscm.commons.spring.common.ApiException;
//import org.junit.Test;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
//import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectoryDtoTest extends AbstractTest {

    @Autowired
    private DirectoryDto dto;
    @Autowired
    private ApplicationProperties properties;

    @Test
    public void testAddDirectory() throws ApiException {
        List<DirectoryData> data = dto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        dto.add(directoryForm);
        data = dto.getAllDirectories();
        assertEquals(2, data.size());
        assertEquals(properties.getRootDirectory(), data.get(0).getDirectoryName());
        assertEquals(0, data.get(0).getParentId().intValue());
        assertEquals("Standard Reports", data.get(1).getDirectoryName());
        assertEquals(data.get(0).getId(), data.get(1).getParentId());
    }

    @Test
    public void testUpdateDirectory() throws ApiException {
        List<DirectoryData> data = dto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        dto.add(directoryForm);
        data = dto.getAllDirectories();
        assertEquals(2, data.size());
        assertEquals(properties.getRootDirectory(), data.get(0).getDirectoryName());
        assertEquals(0, data.get(0).getParentId().intValue());
        assertEquals("Standard Reports", data.get(1).getDirectoryName());
        assertEquals(data.get(0).getId(), data.get(1).getParentId());
        directoryForm = getDirectoryForm("Omni Custom Reports", data.get(0).getId());
        dto.update(data.get(1).getId(), directoryForm);
        data = dto.getAllDirectories();
        assertEquals(2, data.size());
        assertEquals(properties.getRootDirectory(), data.get(0).getDirectoryName());
        assertEquals(0, data.get(0).getParentId().intValue());
        assertEquals("Omni Custom Reports", data.get(1).getDirectoryName());
        assertEquals(data.get(0).getId(), data.get(1).getParentId());
    }
}
