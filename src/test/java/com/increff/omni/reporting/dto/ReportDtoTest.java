package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportQueryForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportDtoTest extends AbstractTest {

    @Autowired
    private ReportDto dto;
    @Autowired
    private DirectoryDto directoryDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private ConnectionDto connectionDto;

    private ReportForm commonSetup(String name, ReportType type) throws ApiException {
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
        return getReportForm(name, type, directoryData.getId(), schemaData.getId());
    }

    @Test
    public void testAdd() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        dto.add(form);
        List<ReportData> reportDataList = dto.selectAll(100001);
        assertEquals(1, reportDataList.size());
        assertEquals("Report 1", reportDataList.get(0).getName());
        assertEquals(ReportType.STANDARD, reportDataList.get(0).getType());
    }

    @Test
    public void testUpdate() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        ReportForm updateForm = getReportForm("Report 3", ReportType.CUSTOM, form.getDirectoryId(), form.getSchemaVersionId());
        dto.edit(data.getId(), updateForm);
        ReportData fData = dto.get(data.getId());
        assertNotNull(fData);
        assertEquals("Report 3", fData.getName());
        assertEquals(ReportType.CUSTOM, fData.getType());
    }

    @Test
    public void testUpsertQuery() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        ReportQueryData queryData = dto.getQuery(data.getId());
        assertEquals("", queryData.getQuery());
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        dto.upsertQuery(data.getId(), queryForm);
        queryData = dto.getQuery(data.getId());
        assertEquals("select version();", queryData.getQuery());
    }

    @Test
    public void testMapToControlAndDelete() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
//        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
//                , InputControlType.TEXT, new ArrayList<>(), null, null);
//        InputControlData data = dto.add(form);
//        dto.mapToControl(data.getId(), );
    }
}
