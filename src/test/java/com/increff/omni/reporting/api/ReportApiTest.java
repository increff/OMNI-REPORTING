package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.increff.commons.springboot.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.jupiter.api.Assertions.*;

public class ReportApiTest extends AbstractTest {

    @Autowired
    private ReportApi api;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private ApplicationProperties properties;

    @Test
    public void testAddReport() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        api.add(pojo);
        ReportPojo r = api.getCheck(pojo.getId());
        assertNotNull(r);
        assertEquals("CIMS Inventory Exposure Report", r.getName());
        assertEquals(ReportType.STANDARD, r.getType());
        assertEquals(directoryPojo.getId(), r.getDirectoryId());
        assertEquals(schemaVersionPojo.getId(), r.getSchemaVersionId());
    }

    @Test
    public void testGetByNameAndSchema() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        api.add(pojo);
        ReportPojo r = api.getByNameAndSchema("CIMS Inventory Exposure Report", schemaVersionPojo.getId(), false);
        assertNotNull(r);
        assertEquals("CIMS Inventory Exposure Report", r.getName());
        assertEquals(ReportType.STANDARD, r.getType());
        assertEquals(directoryPojo.getId(), r.getDirectoryId());
        assertEquals(schemaVersionPojo.getId(), r.getSchemaVersionId());
        assertEquals(schemaVersionPojo.getId(), r.getSchemaVersionId());
        r = api.getByNameAndSchema("CIMS Inventory Exposure Report", schemaVersionPojo.getId(), true);
        assertNull(r);
    }

    @Test
    public void testGetByTypeAndSchema() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(),
                schemaVersionPojo.getId());
        api.add(pojo);
        List<ReportPojo> r = api.getByTypeAndSchema(ReportType.CUSTOM, Collections.singletonList(schemaVersionPojo.getId()), false, null);
        assertEquals(0, r.size());
        r = api.getByTypeAndSchema(ReportType.STANDARD, Collections.singletonList(schemaVersionPojo.getId()), false, null);
        assertEquals(1, r.size());
        assertEquals("CIMS Inventory Exposure Report", r.get(0).getName());
        assertEquals(ReportType.STANDARD, r.get(0).getType());
        assertEquals(directoryPojo.getId(), r.get(0).getDirectoryId());
        assertEquals(schemaVersionPojo.getId(), r.get(0).getSchemaVersionId());
    }

    @Test
    public void testGetByIdsAndSchema() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(),
                schemaVersionPojo.getId());
        api.add(pojo);
        List<ReportPojo> r = api.getByIdsAndSchema(Collections.singletonList(pojo.getId()), Collections.singletonList(schemaVersionPojo.getId()),
                false);
        assertEquals(1, r.size());
        assertEquals("CIMS Inventory Exposure Report", r.get(0).getName());
        assertEquals(ReportType.STANDARD, r.get(0).getType());
        assertEquals(directoryPojo.getId(), r.get(0).getDirectoryId());
        assertEquals(schemaVersionPojo.getId(), r.get(0).getSchemaVersionId());
        r = api.getByIdsAndSchema(Collections.singletonList(pojo.getId()), Collections.singletonList(schemaVersionPojo.getId()),
                true);
        assertEquals(0, r.size());
    }

    @Test
    public void testGetByIdsAndSchemaEmptyList() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(),
                schemaVersionPojo.getId());
        api.add(pojo);
        List<ReportPojo> r = api.getByIdsAndSchema(Collections.emptyList(), Collections.singletonList(schemaVersionPojo.getId()), false);
        assertEquals(0, r.size());
    }

    @Test
    public void testUpdateReport() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo pojo = getReportPojo("CIMS Inventory Exposure Report", ReportType.STANDARD, directoryPojo.getId(),
                schemaVersionPojo.getId());
        api.add(pojo);
        ReportPojo updatePojo =
                getReportPojo("Client CIMS Inventory Exposure Report", ReportType.CUSTOM, rootPojo.getId(),
                        schemaVersionPojo.getId());
        updatePojo.setId(pojo.getId());
        api.edit(updatePojo);
        ReportPojo r = api.getCheck(pojo.getId());
        assertNotNull(r);
        assertEquals("Client CIMS Inventory Exposure Report", r.getName());
        assertEquals(ReportType.CUSTOM, r.getType());
        assertEquals(rootPojo.getId(), r.getDirectoryId());
        assertEquals(schemaVersionPojo.getId(), r.getSchemaVersionId());
    }
}
