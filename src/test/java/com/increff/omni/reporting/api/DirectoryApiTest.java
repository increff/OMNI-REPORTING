package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static org.junit.Assert.assertEquals;

public class DirectoryApiTest extends AbstractTest {

    @Autowired
    private DirectoryApi api;
    @Autowired
    private DirectoryDao dao;
    @Autowired
    private ApplicationProperties properties;

    @Test
    public void testAddDirectory() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(2, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
    }

    @Test(expected = ApiException.class)
    public void testAddDirectoryMissingParentDirectory() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId() + 1);
        try {
            api.add(pojo);
        } catch (ApiException e) {
            List<DirectoryPojo> directoryPojoList = api.getAll();
            assertEquals(1, directoryPojoList.size());
            assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
            assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No directory with id : " + (rootPojo.getId() + 1), e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddDirectoryWithDuplicateName() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo(rootPojo.getDirectoryName(), rootPojo.getId());
        try {
            api.add(pojo);
        } catch (ApiException e) {
            List<DirectoryPojo> directoryPojoList = api.getAll();
            assertEquals(1, directoryPojoList.size());
            assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
            assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Directory already present with same name", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testUpdateOnlyDirectoryName() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        DirectoryPojo pojo2 = getDirectoryPojo("CIMS Reports", pojo.getId());
        api.add(pojo2);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());

        DirectoryPojo pojo3 = getDirectoryPojo("Omni CIMS Reports", pojo.getId());
        pojo3.setId(pojo2.getId());
        api.update(pojo3);
        directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("Omni CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());
    }

    @Test
    public void testUpdateDirectory() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        DirectoryPojo pojo2 = getDirectoryPojo("CIMS Reports", pojo.getId());
        api.add(pojo2);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());

        DirectoryPojo pojo3 = getDirectoryPojo("Omni CIMS Reports", rootPojo.getId());
        pojo3.setId(pojo2.getId());
        api.update(pojo3);
        directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("Omni CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(2).getParentId());
    }

    @Test(expected = ApiException.class)
    public void testUpdateDirectoryInvalidId() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        DirectoryPojo pojo2 = getDirectoryPojo("CIMS Reports", pojo.getId());
        api.add(pojo2);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());

        DirectoryPojo pojo3 = getDirectoryPojo("Omni CIMS Reports", rootPojo.getId());
        pojo3.setId(pojo2.getId()+10);
        try {
            api.update(pojo3);
        } catch (ApiException e) {
            directoryPojoList = api.getAll();
            assertEquals(3, directoryPojoList.size());
            assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
            assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
            assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
            assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
            assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
            assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No directory with id : " + (pojo2.getId()+10), e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testUpdateDirectoryInvalidParentId() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        DirectoryPojo pojo2 = getDirectoryPojo("CIMS Reports", pojo.getId());
        api.add(pojo2);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());

        DirectoryPojo pojo3 = getDirectoryPojo("Omni CIMS Reports", rootPojo.getId() + 10);
        pojo3.setId(pojo2.getId());
        try {
            api.update(pojo3);
        } catch (ApiException e) {
            directoryPojoList = api.getAll();
            assertEquals(3, directoryPojoList.size());
            assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
            assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
            assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
            assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
            assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
            assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No directory with id : " + (rootPojo.getId() + 10), e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testUpdateDirectoryDuplicateName() throws ApiException {
        DirectoryPojo rootPojo = dao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo pojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        api.add(pojo);
        DirectoryPojo pojo2 = getDirectoryPojo("CIMS Reports", pojo.getId());
        api.add(pojo2);
        List<DirectoryPojo> directoryPojoList = api.getAll();
        assertEquals(3, directoryPojoList.size());
        assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
        assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
        assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
        assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
        assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
        assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());

        DirectoryPojo pojo3 = getDirectoryPojo("Standard Reports", rootPojo.getId());
        pojo3.setId(pojo2.getId());
        try {
            api.update(pojo3);
        } catch (ApiException e) {
            directoryPojoList = api.getAll();
            assertEquals(3, directoryPojoList.size());
            assertEquals(properties.getRootDirectory(), directoryPojoList.get(0).getDirectoryName());
            assertEquals(0, directoryPojoList.get(0).getParentId().intValue());
            assertEquals("Standard Reports", directoryPojoList.get(1).getDirectoryName());
            assertEquals(rootPojo.getId(), directoryPojoList.get(1).getParentId());
            assertEquals("CIMS Reports", directoryPojoList.get(2).getDirectoryName());
            assertEquals(pojo.getId(), directoryPojoList.get(2).getParentId());
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Directory already present with same name", e.getMessage());
            throw e;
        }
    }

}
