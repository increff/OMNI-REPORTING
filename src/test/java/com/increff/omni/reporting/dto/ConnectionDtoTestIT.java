package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.*;

public class ConnectionDtoTestIT extends AbstractTest {

    @Autowired
    private ConnectionDto dto;
    @Autowired
    private ReportDto reportDto;
    @Autowired
    private DirectoryDto directoryDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private InputControlDto inputControlDto;

    private ReportForm commonSetup(String name, ReportType type) throws ApiException {
        OrganizationForm form = getOrganizationForm(100001, "increff");
        OrganizationData organizationData = organizationDto.add(form);
        List<DirectoryData> data = directoryDto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        DirectoryData directoryData = directoryDto.add(directoryForm);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        ConnectionForm connectionForm = getConnectionForm("127.0.0.1", "Test DB", username, password);
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.mapToConnection(organizationData.getId(), connectionData.getId());
        organizationDto.mapToSchema(organizationData.getId(), schemaData.getId());
        return getReportForm(name, type, directoryData.getId(), schemaData.getId(), false);
    }

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
    public void testGetLiveData() throws ApiException {
        ReportForm reportForm = commonSetup("Report 2", ReportType.STANDARD);
        reportForm.setIsDashboard(true);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryData queryData = reportDto.getQuery(reportData.getId());
        assertEquals("", queryData.getQuery());
        ReportQueryForm queryForm = getReportQueryForm("select version() as version;");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.NUMBER, new ArrayList<>(), null, null, reportForm.getSchemaVersionId());
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        reportDto.mapToControl(reportData.getId(), inputControlData.getId());
        Map<String, List<String>> params = new HashMap<>();
        params.put("clientId", Collections.singletonList("1100007455"));
        ReportRequestForm form = getReportRequestForm(reportData.getId(), params, "Asia/Kolkata");
        List<Map<String, String>> data = reportDto.getLiveData(form);
        assertEquals(1, data.size());
    }

    @Test
    public void testConnection() throws ApiException {
        ConnectionForm form = getConnectionForm("127.0.0.1", "Test DB", username, password);
        dto.testConnection(form);
    }

    @Test(expected = ApiException.class)
    public void testConnectionWrongPassword() throws ApiException {
        ConnectionForm form = getConnectionForm("127.0.0.1", "Test DB", username, "wrong_password");
        try {
            dto.testConnection(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.UNKNOWN_ERROR, e.getStatus());
            assertTrue(e.getMessage().contains("Database could not be connected"));
            throw e;
        }
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
