package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.*;

public class OrgDtoTest extends AbstractTest {

    @Autowired
    private OrganizationDto dto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private ConnectionDto connectionDto;

    @Test
    public void testOrgCreate() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        dto.add(form);
        OrganizationData data = dto.getById(1);
        assertEquals(1, data.getId().intValue());
        assertEquals("increff", data.getName());
    }

    @Test(expected = ApiException.class)
    public void testOrgCreateValidationFail() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, null);
        try {
            dto.add(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Input validation failed", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testOrgUpdate() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        dto.add(form);
        form = getOrganizationForm(1, "increff2");
        OrganizationData data = dto.update(form);
        assertEquals(1, data.getId().intValue());
        assertEquals("increff2", data.getName());
    }

    @Test
    public void testGetAllOrgs() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        dto.add(form);
        form = getOrganizationForm(2, "increff2");
        dto.add(form);
        List<OrganizationData> organizationDataList = dto.selectAll();
        assertEquals(2, organizationDataList.size());
        assertEquals(1, organizationDataList.get(0).getId().intValue());
        assertEquals(2, organizationDataList.get(1).getId().intValue());
        assertEquals("increff", organizationDataList.get(0).getName());
        assertEquals("increff2", organizationDataList.get(1).getName());
    }

    @Test
    public void testMapToSchema() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        OrganizationData organizationData = dto.add(form);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        OrgSchemaData data = dto.mapToSchema(organizationData.getId(), schemaData.getId());
        assertNotNull(data);
        assertEquals("9.0.1", data.getSchemaName());
        assertEquals(schemaData.getId(), data.getSchemaVersionId());
        assertEquals(1, data.getOrgId().intValue());
    }

    @Test
    public void testGetOrgSchemaData() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        OrganizationData organizationData = dto.add(form);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        OrganizationForm form2 = getOrganizationForm(2, "increff2");
        OrganizationData organizationData2 = dto.add(form2);
        SchemaVersionForm schemaVersionForm2 = getSchemaForm("9.0.2");
        SchemaVersionData schemaData2 = schemaDto.add(schemaVersionForm2);
        dto.mapToSchema(organizationData.getId(), schemaData.getId());
        dto.mapToSchema(organizationData2.getId(), schemaData2.getId());
        List<OrgSchemaData> orgSchemaDataList = dto.selectAllOrgSchema();
        assertEquals(2, orgSchemaDataList.size());
        assertEquals(1, orgSchemaDataList.get(0).getOrgId().intValue());
        assertEquals("9.0.1", orgSchemaDataList.get(0).getSchemaName());
        assertEquals(schemaData.getId(), orgSchemaDataList.get(0).getSchemaVersionId());
        assertNotEquals(schemaData2.getId(), orgSchemaDataList.get(0).getSchemaVersionId());
        assertEquals(2, orgSchemaDataList.get(1).getOrgId().intValue());
        assertEquals("9.0.2", orgSchemaDataList.get(1).getSchemaName());
        assertEquals(schemaData2.getId(), orgSchemaDataList.get(1).getSchemaVersionId());
        assertNotEquals(schemaData.getId(), orgSchemaDataList.get(1).getSchemaVersionId());
    }

    @Test
    public void testMapToConnection() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        OrganizationData organizationData = dto.add(form);
        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        OrgConnectionData data = dto.mapToConnection(organizationData.getId(), connectionData.getId());
        assertNotNull(data);
        assertEquals("Dev DB", data.getConnectionName());
        assertEquals(connectionData.getId(), data.getConnectionId());
        assertEquals(1, data.getOrgId().intValue());
    }

    @Test
    public void testGetOrgConnectionData() throws ApiException {
        OrganizationForm form = getOrganizationForm(1, "increff");
        OrganizationData organizationData = dto.add(form);
        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        OrganizationForm form2 = getOrganizationForm(2, "increff2");
        OrganizationData organizationData2 = dto.add(form2);
        ConnectionForm connectionForm2 =
                getConnectionForm("dev-db-2.increff.com", "Dev DB 2", "db.user2", "db.password2");
        ConnectionData connectionData2 = connectionDto.add(connectionForm2);
        dto.mapToConnection(organizationData.getId(), connectionData.getId());
        dto.mapToConnection(organizationData2.getId(), connectionData2.getId());
        List<OrgConnectionData> orgConnectionDataList = dto.selectAllOrgConnections();
        assertEquals(2, orgConnectionDataList.size());
        assertEquals(1, orgConnectionDataList.get(0).getOrgId().intValue());
        assertEquals("Dev DB", orgConnectionDataList.get(0).getConnectionName());
        assertEquals(connectionData.getId(), orgConnectionDataList.get(0).getConnectionId());
        assertNotEquals(connectionData2.getId(), orgConnectionDataList.get(0).getConnectionId());
        assertEquals(2, orgConnectionDataList.get(1).getOrgId().intValue());
        assertEquals("Dev DB 2", orgConnectionDataList.get(1).getConnectionName());
        assertEquals(connectionData2.getId(), orgConnectionDataList.get(1).getConnectionId());
        assertNotEquals(connectionData.getId(), orgConnectionDataList.get(1).getConnectionId());
    }
}
