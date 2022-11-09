package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
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
        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.mapToConnection(organizationData.getId(), connectionData.getId());
        organizationDto.mapToSchema(organizationData.getId(), schemaData.getId());
        return getReportForm(name, type, directoryData.getId(), schemaData.getId());
    }

    @Test
    public void testAdd() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        ReportData data = dto.add(form);
        List<ReportData> reportDataList = dto.selectAllBySchemaVersion(data.getSchemaVersionId());
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
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        dto.deleteReportControl(data.getId(), inputControlData.getId());
    }

    @Test
    public void testValidationGroups() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 10, ValidationType.MANDATORY
                , Collections.singletonList(inputControlData.getId()));
        dto.addValidationGroup(data.getId(), groupForm);
        List<ValidationGroupData> dataList = dto.getValidationGroups(data.getId());
        assertEquals(1, dataList.size());
        assertEquals("Client Id", dataList.get(0).getControls().get(0));
        assertEquals(ValidationType.MANDATORY, dataList.get(0).getValidationType());
        assertEquals(10, dataList.get(0).getValidationValue().intValue());
        assertEquals("group1", dataList.get(0).getGroupName());
        dto.deleteValidationGroup(data.getId(), "group1");
        dataList = dto.getValidationGroups(data.getId());
        assertEquals(0, dataList.size());
    }

    @Test
    public void testCopyReports() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.STANDARD);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 10, ValidationType.MANDATORY
                , Collections.singletonList(inputControlData.getId()));
        dto.addValidationGroup(data.getId(), groupForm);
        List<ValidationGroupData> dataList = dto.getValidationGroups(data.getId());
        assertEquals(1, dataList.size());
        assertEquals("Client Id", dataList.get(0).getControls().get(0));
        assertEquals(ValidationType.MANDATORY, dataList.get(0).getValidationType());
        assertEquals(10, dataList.get(0).getValidationValue().intValue());
        assertEquals("group1", dataList.get(0).getGroupName());
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.2");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        CopyReportsForm copyReportsForm = new CopyReportsForm();
        copyReportsForm.setOldSchemaVersionId(data.getSchemaVersionId());
        copyReportsForm.setNewSchemaVersionId(schemaData.getId());
        dto.copyReports(copyReportsForm);
        List<ReportData> reportDataList = dto.selectAllBySchemaVersion(schemaData.getId());
        assertEquals(1, reportDataList.size());
    }

    @Test(expected = ApiException.class)
    public void testAddValidationGroupErrorCase1() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 10, ValidationType.MANDATORY
                , Collections.singletonList(inputControlData.getId()));
        try {
            dto.addValidationGroup(null, groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report id cannot be null", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddValidationGroupErrorCase2() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 10, ValidationType.MANDATORY
                , Arrays.asList(inputControlData.getId(), inputControlData.getId()));
        try {
            dto.addValidationGroup(data.getId(), groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Validation group contains duplicate control ids", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddValidationGroupErrorCase3() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null);
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0, ValidationType.DATE_RANGE
                , Collections.singletonList(inputControlData.getId()));
        try {
            dto.addValidationGroup(data.getId(), groupForm);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Date range validation should have positive validation value", e.getMessage());
            throw e;
        }
    }
}
