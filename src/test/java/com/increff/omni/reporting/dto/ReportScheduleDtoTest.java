package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getInputParamList;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getReportScheduleForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.*;

public class ReportScheduleDtoTest extends AbstractTest {

    @Autowired
    private ReportScheduleDto dto;
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

    private ReportForm commonSetup(Boolean canSchedule) throws ApiException {
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
        return getReportForm("Report 1", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), canSchedule);
    }

    @Test(expected = ApiException.class)
    public void testAddWithScheduleNotAllowed() throws ApiException {
        ReportForm reportForm = commonSetup(false);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 1", "Asia/Kolkata",
                 true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        try {
            dto.scheduleReport(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report : Report 1 is not allowed to schedule", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAdd() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        dto.scheduleReport(form);
        List<ReportScheduleData> reportScheduleData = dto.getScheduleReports(1, 100);
        assertEquals(1, reportScheduleData.size());
        assertEquals("*", reportScheduleData.get(0).getCronSchedule().getDayOfMonth());
        assertEquals("*", reportScheduleData.get(0).getCronSchedule().getHour());
        assertEquals("*/15", reportScheduleData.get(0).getCronSchedule().getMinute());
        assertEquals(0, reportScheduleData.get(0).getSuccessCount().intValue());
        assertEquals(0, reportScheduleData.get(0).getFailureCount().intValue());
        assertEquals("Report 1", reportScheduleData.get(0).getReportName());
        assertTrue(reportScheduleData.get(0).getIsEnabled());
        assertEquals("Asia/Kolkata", reportScheduleData.get(0).getTimezone());
    }

    @Test
    public void testEdit() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        dto.scheduleReport(form);
        ReportScheduleData reportScheduleData = dto.getScheduleReports(1, 100).get(0);
        form.getCronSchedule().setMinute("*/30");
        dto.editScheduleReport(reportScheduleData.getId(), form);
        reportScheduleData = dto.getScheduleReports(1, 100).get(0);
        assertNotNull(reportScheduleData);
        assertEquals("*", reportScheduleData.getCronSchedule().getDayOfMonth());
        assertEquals("*", reportScheduleData.getCronSchedule().getHour());
        assertEquals("*/30", reportScheduleData.getCronSchedule().getMinute());
        assertEquals(0, reportScheduleData.getSuccessCount().intValue());
        assertEquals(0, reportScheduleData.getFailureCount().intValue());
        assertEquals("Report 1", reportScheduleData.getReportName());
        assertTrue(reportScheduleData.getIsEnabled());
        assertEquals("Asia/Kolkata", reportScheduleData.getTimezone());
    }

    @Test
    public void testUpdateStatus() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        dto.scheduleReport(form);
        ReportScheduleData reportScheduleData = dto.getScheduleReports(1, 100).get(0);
        dto.updateStatus(reportScheduleData.getId(), false);
        List<ReportScheduleData> reportScheduleDataList = dto.getScheduleReports(1, 100);
        assertEquals(1, reportScheduleDataList.size());
        assertFalse(reportScheduleDataList.get(0).getIsEnabled());
    }

    @Test
    public void testDeleteSchedule() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        dto.scheduleReport(form);
        ReportScheduleData reportScheduleData = dto.getScheduleReports(1, 100).get(0);
        dto.deleteSchedule(reportScheduleData.getId());
        List<ReportScheduleData> reportScheduleDataList = dto.getScheduleReportsForAllOrgs(1, 100);
        assertEquals(0, reportScheduleDataList.size());
    }


}
