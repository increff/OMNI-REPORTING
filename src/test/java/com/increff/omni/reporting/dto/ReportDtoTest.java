package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
import com.increff.omni.reporting.model.constants.*;
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
        dto.setEncryptionClient(encryptionClient);
        connectionDto.setEncryptionClient(encryptionClient);
        inputControlDto.setEncryptionClient(encryptionClient);
        OrganizationForm form = getOrganizationForm(100001, "increff");
        OrganizationData organizationData = organizationDto.add(form);
        List<DirectoryData> data = directoryDto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        DirectoryData directoryData = directoryDto.add(directoryForm);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        ConnectionForm connectionForm = getConnectionForm("127.0.0.1", "Test DB", username, password);
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaData.getId(), connectionData.getId()));
        return getReportForm(name, type, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
    }

    @Test
    public void testAdd() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        ReportData data = dto.add(form);
        List<ReportData> reportDataList = dto.selectAllBySchemaVersion(data.getSchemaVersionId(), null);
        assertEquals(1, reportDataList.size());
        assertEquals("Report 1", reportDataList.get(0).getName());
        assertEquals(ReportType.STANDARD, reportDataList.get(0).getType());
    }

    @Test
    public void testUpdate() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        ReportForm updateForm =
                getReportForm("Report 3", ReportType.CUSTOM, form.getDirectoryId(), form.getSchemaVersionId(), false, ChartType.REPORT);
        dto.edit(data.getId(), updateForm);
        ReportData fData = dto.get(data.getId());
        assertNotNull(fData);
        assertEquals("Report 3", fData.getName());
        assertEquals(ReportType.CUSTOM, fData.getType());
    }

    @Test
    public void testUpdateStatus() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        assertEquals(true, data.getIsEnabled());
        dto.updateStatus(data.getId(), false);
        ReportData fData = dto.get(data.getId());
        assertNotNull(fData);
        assertEquals(ReportType.CUSTOM, fData.getType());
        assertEquals(false, fData.getIsEnabled());
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
    public void testTransformedQuery() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("select * from table where id = '1';", queryData.getQuery());
    }

    @Test
    public void testTransformedQueryWithWrongParam() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setParamMap(new HashMap<>());
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("select * from table where id = <<replace(id)>>;", queryData.getQuery());
    }

    @Test
    public void testTransformedQueryWithWrongParamCase2() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setParamMap(new HashMap<>());
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("select * from table where id = <<replace(id)>>;", queryData.getQuery());
    }

    @Test
    public void testTransformedQueryCase2() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("select * from table where <<filter(id,id,<=)>>;");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("select * from table where id <= '1';", queryData.getQuery());
    }

    @Test
    public void testMongoFilterKeepQuotesFalse() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoFilter(id, { id_column : #id }, keepQuotesFalse)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("{ id_column : 1 }", queryData.getQuery());
    }

    @Test
    public void testMongoFilterKeepQuotesTrue() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoFilter(id, { id_column : #id }, keepQuotesTrue)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("{ id_column : '1' }", queryData.getQuery());
    }

    @Test
    public void testMongoFilterNoValue() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoFilter(key_wout_val, { id_column : #key_wout_val }, keepQuotesTrue)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("{}", queryData.getQuery());
    }

    @Test
    public void testMongoReplaceKeepQuotesFalse() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoReplace(id, keepQuotesFalse)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("1", queryData.getQuery());
    }

    @Test
    public void testMongoReplaceKeepQuotesTrue() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoReplace(id, keepQuotesTrue)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("'1'", queryData.getQuery());
    }

    @Test
    public void testMongoReplaceNoValue() throws ApiException {
        ReportQueryTestForm testForm = getQueryTestForm();
        testForm.setQuery("<<mongoReplace(id, keepQuotesTrue)>>");
        ReportQueryData queryData = dto.getTransformedQuery(testForm);
        assertEquals("'1'", queryData.getQuery());
    }

    @Test
    public void testMapToControlAndDelete() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        dto.deleteReportControl(data.getId(), inputControlData.getId());
    }

    @Test
    public void testValidationGroups() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
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
        assertFalse(dataList.get(0).getIsSystemValidation());
        dto.deleteValidationGroup(data.getId(), "group1");
        dataList = dto.getValidationGroups(data.getId());
        assertEquals(0, dataList.size());
    }

    @Test
    public void testCopyReports() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.STANDARD);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
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
        List<ReportData> reportDataList = dto.selectAllBySchemaVersion(schemaData.getId(), null);
        assertEquals(1, reportDataList.size());
    }

    @Test(expected = ApiException.class)
    public void testAddValidationGroupErrorCase1() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
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
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
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
                , InputControlType.TEXT, new ArrayList<>(), null, null, form.getSchemaVersionId());
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

    @Test
    public void testAutoValidationGroupAddition() throws ApiException {
        ReportForm form = commonSetup("Report 2", ReportType.CUSTOM);
        ReportData data = dto.add(form);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.ACCESS_CONTROLLED_MULTI_SELECT, Arrays.asList("IGNORE", "SENT"), null, null,
                form.getSchemaVersionId());
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        dto.mapToControl(data.getId(), inputControlData.getId());
        List<ValidationGroupData> validationGroupData = dto.getValidationGroups(data.getId());
        assertEquals(1, validationGroupData.size());
        assertTrue(validationGroupData.get(0).getIsSystemValidation());
    }

    @Test
    public void testAddTableChartWithoutLegends() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        form.setIsChart(true);
        form.setChartType(ChartType.TABLE);
        dto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddChartWithIsChartFalse() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        form.setIsChart(false);
        form.setChartType(ChartType.TABLE);
        dto.add(form);
    }

    @Test(expected = ApiException.class)
    public void testAddBarChartWithoutLegends() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        form.setIsChart(true);
        form.setChartType(ChartType.BAR);
        dto.add(form);
    }

    @Test
    public void testAddBarChartWithLegends() throws ApiException {
        ReportForm form = commonSetup("Report 1", ReportType.STANDARD);
        form.setIsChart(true);
        form.setChartType(ChartType.BAR);
        HashMap<String, String> legends = new HashMap<>();
        legends.put("Xkey", "Xvalue");
        legends.put("Ykey", "Yvalue");
        form.setLegends(legends);
        dto.add(form);
    }


}
