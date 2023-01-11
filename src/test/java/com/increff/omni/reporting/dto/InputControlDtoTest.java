package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ReportApi;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.DateType;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.data.OrganizationData;
import com.increff.omni.reporting.model.data.ValidationGroupData;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlUpdateForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportPojo;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.*;

public class InputControlDtoTest extends AbstractTest {

    @Autowired
    private InputControlDto dto;
    @Autowired
    private OrganizationDto organizationDto;
    @Autowired
    private ConnectionDto connectionDto;
    @Autowired
    private SchemaDto schemaDto;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private ReportDto reportDto;

    Integer schemaVersionId;

    private void commonSetup() throws ApiException {
        OrganizationForm organizationForm = getOrganizationForm(100001, "increff");
        OrganizationData organizationData = organizationDto.add(organizationForm);
        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        SchemaVersionForm form = getSchemaForm("9.0.1");
        schemaVersionId = schemaDto.add(form).getId();
        organizationDto.mapToConnection(organizationData.getId(), connectionData.getId());
    }

    @Test
    public void testAddInputControl() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData data = dto.add(form);
        assertEquals("Client Id", data.getDisplayName());
        assertEquals("clientId", data.getParamName());
        assertEquals(InputControlScope.GLOBAL, data.getScope());
        assertEquals(InputControlType.TEXT, data.getType());
        assertNull(data.getQuery());
        assertEquals(0, data.getOptions().size());
    }

    @Test(expected = ApiException.class)
    public void testAddInputControlForInvalidQuery() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), "select version();", null, schemaVersionId);
        try {
            dto.add(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("For Text, Number and Date, neither query nor value is needed", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddInputControlForWrongMultiSelectCase1() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.MULTI_SELECT, new ArrayList<>(), "", null, schemaVersionId);
        try {
            dto.add(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("For Select, either query or value is mandatory", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddInputControlForWrongMultiSelectCase2() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.MULTI_SELECT, Arrays.asList("Live", "Packed"), "select version();", null, schemaVersionId);
        try {
            dto.add(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("For Select, either query or value one is mandatory", e.getMessage());
            throw e;
        }
    }

    @Test(expected = ApiException.class)
    public void testAddLocalInputControl() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.LOCAL
                , InputControlType.MULTI_SELECT, new ArrayList<>(), "select version();", null, schemaVersionId);
        try {
            dto.add(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report is mandatory for Local Scope Control", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testUpdateControl() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData data1 = dto.add(form);
        InputControlUpdateForm updateForm = getInputControlUpdateForm("Client id 2", "clientId2"
        , InputControlType.DATE, new ArrayList<>(), null);
        updateForm.setDateType(DateType.END_DATE);
        dto.update(data1.getId(), updateForm);
        InputControlData data = dto.getById(data1.getId());
        assertEquals("Client id 2", data.getDisplayName());
        assertEquals("clientId2", data.getParamName());
        assertEquals(InputControlScope.GLOBAL, data.getScope());
        assertEquals(InputControlType.DATE, data.getType());
        assertEquals(DateType.END_DATE, data.getDateType());
        assertNull(data.getQuery());
        assertEquals(0, data.getOptions().size());
    }

    @Test
    public void testUpdateControlDateType() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.DATE, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData data1 = dto.add(form);
        assertEquals(DateType.START_DATE, data1.getDateType());
        InputControlUpdateForm updateForm = getInputControlUpdateForm("Client id 2", "clientId2"
                , InputControlType.DATE_TIME, new ArrayList<>(), null);
        updateForm.setDateType(DateType.END_DATE);
        dto.update(data1.getId(), updateForm);
        InputControlData data = dto.getById(data1.getId());
        assertEquals("Client id 2", data.getDisplayName());
        assertEquals("clientId2", data.getParamName());
        assertEquals(InputControlScope.GLOBAL, data.getScope());
        assertEquals(InputControlType.DATE_TIME, data.getType());
        assertEquals(DateType.END_DATE, data.getDateType());
        assertNull(data.getQuery());
        assertEquals(0, data.getOptions().size());
    }

    @Test
    public void testSelect() throws ApiException {
        commonSetup();
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.DATE, new ArrayList<>(), null, null, schemaVersionId);
        dto.add(form);
        ReportPojo pojo = getReportPojo("report 1", ReportType.STANDARD, 100001, schemaVersionId);
        reportApi.add(pojo);
        form = getInputControlForm("Client Id 2", "clientId2", InputControlScope.LOCAL
                , InputControlType.SINGLE_SELECT, Arrays.asList("IGNORE", "SENT"), null, pojo.getId(), schemaVersionId);
        dto.add(form);
        List<ValidationGroupData> validationGroupData = reportDto.getValidationGroups(pojo.getId());
        assertEquals(1, validationGroupData.size());
        assertTrue(validationGroupData.get(0).getIsSystemValidation());
        List<InputControlData> inputControlDataList = dto.selectAllGlobal(pojo.getSchemaVersionId());
        assertEquals(1, inputControlDataList.size());
        InputControlData data = inputControlDataList.get(0);
        assertEquals("Client Id", data.getDisplayName());
        assertEquals("clientId", data.getParamName());
        assertEquals(InputControlScope.GLOBAL, data.getScope());
        assertEquals(InputControlType.DATE, data.getType());
        assertNull(data.getQuery());
        assertEquals(0, data.getOptions().size());
        inputControlDataList = dto.selectForReport(pojo.getId());
        assertEquals(1, inputControlDataList.size());
        data = inputControlDataList.get(0);
        assertEquals("Client Id 2", data.getDisplayName());
        assertEquals("clientId2", data.getParamName());
        assertEquals("9.0.1", data.getSchemaVersionName());
        assertEquals(InputControlScope.LOCAL, data.getScope());
        assertEquals(InputControlType.SINGLE_SELECT, data.getType());
        assertNull(data.getQuery());
        assertEquals(2, data.getOptions().size());

    }
}
