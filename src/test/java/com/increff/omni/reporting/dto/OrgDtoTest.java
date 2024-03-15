package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
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
        connectionDto.setEncryptionClient(encryptionClient);
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

}
