package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.DirectoryApi;
import com.increff.omni.reporting.api.InputControlApi;
import com.increff.omni.reporting.api.ReportScheduleApi;
import com.increff.omni.reporting.api.SchemaVersionApi;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.ValidationGroupForm;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.*;

import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlPojo;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlQueryPojo;
import static com.increff.omni.reporting.helper.ReportScheduleTestHelper.*;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaPojo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReportScheduleFlowApiTest extends AbstractTest {

    @Autowired
    private ReportScheduleFlowApi flowApi;
    @Autowired
    private DirectoryDao directoryDao;
    @Autowired
    private ApplicationProperties properties;
    @Autowired
    private DirectoryApi directoryApi;
    @Autowired
    private SchemaVersionApi schemaVersionApi;
    @Autowired
    private ReportFlowApi reportFlowApi;
    @Autowired
    private InputControlApi inputControlApi;
    @Autowired
    private ReportScheduleApi reportScheduleApi;

    private ReportPojo commonSetup() throws ApiException {
        DirectoryPojo rootPojo = directoryDao.select("directoryName", properties.getRootDirectory());
        DirectoryPojo directoryPojo = getDirectoryPojo("Standard Reports", rootPojo.getId());
        directoryApi.add(directoryPojo);
        SchemaVersionPojo schemaVersionPojo = getSchemaPojo("9.0.1");
        schemaVersionApi.add(schemaVersionPojo);
        ReportPojo reportPojo = getReportPojo("Report 1", ReportType.STANDARD
                , directoryPojo.getId(), schemaVersionPojo.getId());
        ReportPojo pojo = reportFlowApi.addReport(reportPojo, new HashMap<>());
        assertNotNull(pojo);
        assertEquals(ReportType.STANDARD, pojo.getType());
        assertEquals("Report 1", pojo.getName());
        InputControlPojo inputControlPojo = getInputControlPojo("Client ID", "clientId"
                , InputControlScope.GLOBAL, InputControlType.MULTI_SELECT, schemaVersionPojo.getId());
        InputControlQueryPojo inputControlQueryPojo = getInputControlQueryPojo("select * from oms.oms_orders;"
                , null);
        inputControlApi.add(inputControlPojo, inputControlQueryPojo, new ArrayList<>());
        ReportControlsPojo controlsPojo = getReportControlsPojo(reportPojo.getId(), inputControlPojo.getId());
        reportFlowApi.mapControlToReport(controlsPojo);
        ValidationGroupForm groupForm = getValidationGroupForm("group1", 0
                , ValidationType.MANDATORY, Collections.singletonList(inputControlPojo.getId()));
        reportFlowApi.addValidationGroup(reportPojo.getId(), groupForm);
        return reportPojo;
    }

    @Test
    public void testAdd() throws ApiException {
        ReportPojo reportPojo = commonSetup();
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        flowApi.add(schedulePojo, Arrays.asList("a@gmail.com", "b@gmail.com"), Collections.singletonList(paramsPojo),
                reportPojo);
        ReportSchedulePojo schedulePojo1 = reportScheduleApi.getCheck(schedulePojo.getId());
        assertNotNull(schedulePojo1);
        assertEquals(true, schedulePojo1.getIsEnabled());
        assertEquals(false, schedulePojo1.getIsDeleted());
        assertEquals("0 */15 * * * ?", schedulePojo1.getCron());
        List<ReportScheduleEmailsPojo> emailsPojoList = reportScheduleApi.getByScheduleId(schedulePojo.getId());
        assertEquals(2, emailsPojoList.size());
        assertEquals("a@gmail.com", emailsPojoList.get(0).getSendTo());
        assertEquals("b@gmail.com", emailsPojoList.get(1).getSendTo());
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(1, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Client ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002253'", scheduleInputParamsPojoList.get(0).getParamValue());
    }

    @Test(expected = ApiException.class)
    public void testAddWithNoValidEmail() throws ApiException {
        ReportPojo reportPojo = commonSetup();
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        try {
            flowApi.add(schedulePojo, Arrays.asList("a.gmail.com", "b.gmail.com"), Collections.singletonList(paramsPojo),
                    reportPojo);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("No valid emails given, [\"a.gmail.com\",\"b.gmail.com\"]",e.getMessage());
            throw e;
        }
    }

    @Test
    public void editSchedule() throws ApiException {
        ReportPojo reportPojo = commonSetup();
        ReportSchedulePojo schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */15 * * * ?");
        ReportScheduleInputParamsPojo paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002253'", "Client ID");
        flowApi.add(schedulePojo, Arrays.asList("a@gmail.com", "b@gmail.com"), Collections.singletonList(paramsPojo),
                reportPojo);
        Integer id = schedulePojo.getId();
        schedulePojo = getReportSchedulePojo("Report 1", true, false, 0, 10, ZonedDateTime.now(),
                100001, 100001, "0 */20 * * * ?");
        schedulePojo.setId(id);
        paramsPojo = getReportScheduleInputParamsPojo(schedulePojo.getId(), "clientId",
                "'1100002254'", "Wh ID");
        flowApi.edit(schedulePojo, Arrays.asList("a@gmail.com", "b.gmail.com"), Collections.singletonList(paramsPojo),
                reportPojo);
        ReportSchedulePojo schedulePojo1 = reportScheduleApi.getCheck(schedulePojo.getId());
        assertNotNull(schedulePojo1);
        assertEquals(true, schedulePojo1.getIsEnabled());
        assertEquals(false, schedulePojo1.getIsDeleted());
        assertEquals("0 */20 * * * ?", schedulePojo1.getCron());
        List<ReportScheduleEmailsPojo> emailsPojoList = reportScheduleApi.getByScheduleId(schedulePojo.getId());
        assertEquals(1, emailsPojoList.size());
        assertEquals("a@gmail.com", emailsPojoList.get(0).getSendTo());
        List<ReportScheduleInputParamsPojo> scheduleInputParamsPojoList =
                reportScheduleApi.getScheduleParams(schedulePojo.getId());
        assertEquals(1, scheduleInputParamsPojoList.size());
        assertEquals("clientId", scheduleInputParamsPojoList.get(0).getParamKey());
        assertEquals("Wh ID", scheduleInputParamsPojoList.get(0).getDisplayValue());
        assertEquals("'1100002254'", scheduleInputParamsPojoList.get(0).getParamValue());
    }

}
