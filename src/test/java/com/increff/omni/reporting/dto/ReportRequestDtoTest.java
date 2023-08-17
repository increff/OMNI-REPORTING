package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportRequestStatus;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.assertEquals;

public class ReportRequestDtoTest extends AbstractTest {

    @Autowired
    private ReportRequestDto dto;
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

    private ReportForm commonSetup() throws ApiException {
        dto.setEncryptionClient(encryptionClient);
        reportDto.setEncryptionClient(encryptionClient);
        connectionDto.setEncryptionClient(encryptionClient);
        inputControlDto.setEncryptionClient(encryptionClient);
        OrganizationForm form = getOrganizationForm(100001, "increff");
        OrganizationData organizationData = organizationDto.add(form);
        List<DirectoryData> data = directoryDto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        DirectoryData directoryData = directoryDto.add(directoryForm);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.mapToConnection(organizationData.getId(), connectionData.getId());
        organizationDto.mapToSchema(organizationData.getId(), schemaData.getId());
        return getReportForm("Report 2", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false);
    }

    @Test
    public void testAddReportRequest() throws ApiException, IOException {
        ReportForm reportForm = commonSetup();
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryData queryData = reportDto.getQuery(reportData.getId());
        assertEquals("", queryData.getQuery());
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.NUMBER, new ArrayList<>(), null, null, reportForm.getSchemaVersionId());
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        reportDto.mapToControl(reportData.getId(), inputControlData.getId());
        Map<String, List<String>> params = new HashMap<>();
        params.put("clientId", Collections.singletonList("1100007455"));
        ReportRequestForm form = getReportRequestForm(reportData.getId(), params, "Asia/Kolkata");
        dto.requestReport(form);
        List<ReportRequestData> dataList = dto.getAll();
        assertEquals(1, dataList.size());
        assertEquals(reportData.getId(), dataList.get(0).getReportId());
        assertEquals(reportData.getName(), dataList.get(0).getReportName());
        assertEquals(ReportRequestStatus.NEW, dataList.get(0).getStatus());
    }

    @Test
    public void testGetAllTimezones() throws ApiException {
        dto.getAllAvailableTimeZones();
    }
}
