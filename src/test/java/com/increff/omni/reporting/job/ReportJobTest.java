package com.increff.omni.reporting.job;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dao.ReportScheduleDao;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getInputParamList;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.getReportScheduleForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReportJobTest extends AbstractTest {

    @Autowired
    private ScheduledJobs reportJob;
    @Autowired
    private ReportRequestDto dto;
    @Autowired
    private ReportScheduleDto scheduleDto;
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

    private ReportForm commonSetup() throws ApiException {
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
        return getReportForm("Report 2", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false);
    }

    @Test
    public void testDeleteFiles() {
        reportJob.deleteOldFiles();
    }

    @Test
    public void testMarkStuck() {
        reportJob.markJobsStuck();
    }

    @Test
    public void testRunReport()
            throws ApiException{
        ReportForm reportForm = commonSetup();
        ReportData reportData = reportDto.add(reportForm);
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
        reportJob.runUserReports();
    }

    @Test
    public void testCreateScheduleRequestWithNoSchedule() throws ApiException {
        commonSetup();
        reportJob.addScheduleReportRequests();
    }

    @Test
    public void testCreateScheduleRequests() throws ApiException {
        ReportForm reportForm = commonSetup();
        reportForm.setCanSchedule(true);
        reportDto.add(reportForm);
        List<ReportScheduleForm.InputParamMap> inputParamMaps = getInputParamList();
        ReportScheduleForm form = getReportScheduleForm("*/15", "*", "*", "Report 2", "Asia/Kolkata",
                true, Arrays.asList("a@gmail.com", "b@gmail.com"), inputParamMaps);
        scheduleDto.scheduleReport(form);
        List<ReportScheduleData> reportScheduleData = scheduleDto.getScheduleReports(1, 100);

        ReportSchedulePojo pojo = reportScheduleDao.select(reportScheduleData.get(0).getId());
        pojo.setNextRuntime(ZonedDateTime.now().minusMinutes(1));
        reportJob.addScheduleReportRequests();
        List<ReportRequestData> dataList = scheduleDto.getScheduledRequests(1, 100);
        assertEquals(1, dataList.size());
        pojo = reportScheduleDao.select(reportScheduleData.get(0).getId());
        assertTrue(ZonedDateTime.now().isBefore(pojo.getNextRuntime()));
    }

}
