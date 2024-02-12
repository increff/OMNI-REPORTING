package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.job.ScheduledJobs;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.PipelineTestHelper.*;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getInputParamList;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getReportScheduleForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportQueryForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.hamcrest.CoreMatchers.containsString;
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
    @Autowired
    private ReportScheduleDao reportScheduleDao;
    @Autowired
    private ScheduledJobs scheduledJobs;
    @Autowired
    private PipelineDto pipelineDto;

    private ReportForm commonSetup(Boolean canSchedule) throws ApiException {
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
        return getReportForm("Report 1", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), canSchedule, ChartType.REPORT);
    }

    @Test(expected = ApiException.class)
    public void testAddWithScheduleNotAllowed() throws ApiException {
        ReportForm reportForm = commonSetup(false);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
        try {
            dto.scheduleReport(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report : report_1 is not allowed to schedule", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testAdd() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
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
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
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
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
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
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
        dto.scheduleReport(form);
        ReportScheduleData reportScheduleData = dto.getScheduleReports(1, 100).get(0);
        dto.deleteSchedule(reportScheduleData.getId());
        List<ReportScheduleData> reportScheduleDataList = dto.getScheduleReportsForAllOrgs(1, 100);
        assertEquals(0, reportScheduleDataList.size());
    }

    @Test
    public void testGetScheduleRequests() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportForm.setCanSchedule(true);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryData queryData = reportDto.getQuery(reportData.getId());
        assertEquals("", queryData.getQuery());
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        InputControlForm inputControlForm = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.MULTI_SELECT, Arrays.asList("1100002253", "1100002255"), null, null,
                reportForm.getSchemaVersionId());
        InputControlData inputControlData = inputControlDto.add(inputControlForm);
        reportDto.mapToControl(reportData.getId(), inputControlData.getId());
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
        dto.scheduleReport(form);
        List<ReportScheduleData> reportScheduleData = dto.getScheduleReports(1, 100);

        ReportSchedulePojo pojo = reportScheduleDao.select(reportScheduleData.get(0).getId());
        pojo.setNextRuntime(ZonedDateTime.now().minusMinutes(1));
        scheduledJobs.addScheduleReportRequests();
        List<ReportRequestData> dataList = dto.getScheduledRequests(1, 100);
        assertEquals(1, dataList.size());
        assertFalse(dataList.get(0).getFilters().isEmpty());
        pojo = reportScheduleDao.select(reportScheduleData.get(0).getId());
        assertTrue(ZonedDateTime.now().isBefore(pojo.getNextRuntime()));
        pojo.setReportAlias("dummy");
        pojo.setNextRuntime(ZonedDateTime.now().minusMinutes(1));
        scheduledJobs.addScheduleReportRequests();
        dataList = dto.getScheduledRequests(1, 100);
        assertEquals(2, dataList.size());
        assertTrue(dataList.get(0).getFilters().isEmpty());
    }

    @Test(expected = ApiException.class)
    public void testAddScheduleWithFrequencyLessThanMinimumAllowedFrequency() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportForm.setMinFrequencyAllowedSeconds(86400);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/5", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
        dto.scheduleReport(form);
    }

    @Test(expected = ApiException.class)
    public void testEditReportMinFrequencyWithConflictingSchedulerFrequency() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        reportForm.setAlias(reportForm.getAlias().toLowerCase());
        reportForm.setMinFrequencyAllowedSeconds(300);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/5", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, new ArrayList<>());
        dto.scheduleReport(form);

        reportForm.setMinFrequencyAllowedSeconds(301);
        reportDto.edit(reportData.getId(), reportForm);
    }

    @Test
    public void testAddWithPipeline() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();

        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        PipelineData pipelineData = pipelineDto.add(pipelineForm);

        PipelineDetailsForm pipelineDetails = getPipelineDetailsForm(pipelineData.getId(), "folder 1");
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, new ArrayList<>(), inputParamMaps, Arrays.asList(pipelineDetails));
        dto.scheduleReport(form);
        List<ReportScheduleData> reportScheduleData = dto.getScheduleReports(1, 100);
        assertEquals(1, reportScheduleData.size());
        assertEquals(reportScheduleData.get(0).getPipelineDetails().get(0).getFolderName(), pipelineDetails.getFolderName());
        assertEquals(reportScheduleData.get(0).getPipelineDetails().get(0).getPipelineId(), pipelineDetails.getPipelineId());
    }

    @Test
    public void testAddWithPipelineAndEmail() throws ApiException {
        ReportForm reportForm = commonSetup(true);
        ReportData reportData = reportDto.add(reportForm);
        ReportQueryForm queryForm = getReportQueryForm("select version();");
        reportDto.upsertQuery(reportData.getId(), queryForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();

        PipelineForm pipelineForm = getPipelineForm("Pipeline 1", PipelineType.GCP, getGCPPipelineConfig("bucket.com", "testBucket", "abc"));
        PipelineData pipelineData = pipelineDto.add(pipelineForm);

        PipelineDetailsForm pipelineDetails = getPipelineDetailsForm(pipelineData.getId(), "folder 1");
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "?", "Report 1", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps, Arrays.asList(pipelineDetails));
        try {
            dto.scheduleReport(form);
        } catch (ApiException e) {
            assertThat(e.getMessage().toLowerCase(), containsString("Only one of email or pipeline".toLowerCase()));
        }

    }
}
