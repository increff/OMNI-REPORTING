package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.api.ReportControlsApi;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.increff.omni.reporting.pojo.ReportControlsPojo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DashboardDtoTestHelper.*;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.*;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.jupiter.api.Assertions.*;


public class DashboardDtoTest extends AbstractTest {

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
    private DashboardDto dashboardDto;
    @Autowired
    private DashboardChartDto dashboardChartDto;
    @Autowired
    private DashboardChartApi dashboardChartApi;
    @Autowired
    private DefaultValueApi defaultValueApi;
    @Autowired
    private ReportFlowApi reportFlowApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private DashboardApi dashboardApi;
    @Autowired
    private ApplicationProperties properties;

    private final Integer orgId = 100001;


    Integer schemaVersionId;
    Integer connectionId;

    private List<ReportData> commonSetup(ReportType type) throws ApiException {
        reportDto.setEncryptionClient(encryptionClient);
        connectionDto.setEncryptionClient(encryptionClient);
        inputControlDto.setEncryptionClient(encryptionClient);
        OrganizationForm form = getOrganizationForm(orgId, "increff");
        OrganizationData organizationData = organizationDto.add(form);
        List<DirectoryData> data = directoryDto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        DirectoryData directoryData = directoryDto.add(directoryForm);
        SchemaVersionForm schemaVersionForm = getSchemaForm("9.0.1");
        SchemaVersionData schemaData = schemaDto.add(schemaVersionForm);
        schemaVersionId = schemaData.getId();
        ConnectionForm connectionForm = getConnectionForm("127.0.0.1", "Test DB", username, password);
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaData.getId(), connectionData.getId()));
        connectionId = connectionData.getId();
        List<ReportForm> forms = new ArrayList<>();
        List<ReportData> datas = new ArrayList<>();
        HashMap<String, String> legends = new HashMap<>();
        legends.put("Xkey", "Xvalue");
        legends.put("Ykey", "Yvalue");

        forms.add(getChartForm("Chart_1", type, directoryData.getId(), schemaData.getId(), false, ChartType.TABLE, legends));
        datas.add(reportDto.add(forms.get(0)));
        forms.add(getChartForm("Chart_2", type, directoryData.getId(), schemaData.getId(), false, ChartType.TABLE, legends));
        datas.add(reportDto.add(forms.get(1)));
        forms.add(getChartForm("Chart_3", type, directoryData.getId(), schemaData.getId(), false, ChartType.TABLE, legends));
        datas.add(reportDto.add(forms.get(2)));
        return datas;

    }

    @Test
    public void testAdd() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
    }

    @Test
    public void testExceedDashboardsPerOrgMaxLimit() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        for(int i = 0; i<properties.getMaxDashboardsPerOrg() + 1; i++) {
            if(i == properties.getMaxDashboardsPerOrg()) {
                int finalI = i;
                ApiException exception = assertThrows(ApiException.class, () -> {
                    dashboardDto.addDashboard(getDashboardAddForm("Dashboard_"+ finalI,
                            Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
                });
                assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
                assertTrue(exception.getMessage().contains("Max limit of dashboards reached: " + properties.getMaxDashboardsPerOrg()));
                break;
            }
            DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_"+i,
                    Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        }
    }

    @Test
    public void testUpdate() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1", Collections.singletonList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        DashboardForm form = getDashboardForm("Dashboard_2");
        dashboardDto.updateDashboard(form, data.getId());
        assertEquals(dashboardDto.getDashboardsByOrgId().get(0).getName(), "Dashboard_2");
    }

    @Test
    public void testSameChartTwiceInDashboardError() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        ApiException exception = assertThrows(ApiException.class, () -> {
          dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
            Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
               getDashboardChartForm(chartDatas.get(0).getAlias(), 1, 0, 0, RowHeight.HALF))));
        });
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Same chart cannot be added twice. Duplicate chart alias: chart_1", exception.getMessage());
    }

    @Test
    public void testGet() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
    }

    @Test
    public void testGetDashboardWithDifferentOrg() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(100002);
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
        ApiException exception = assertThrows(ApiException.class, () -> {
            dashboardDto.getDashboard(data.getId());
        });
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Dashboard does not belong to orgId: 100002", exception.getMessage());
    }

    @Test
    public void testGetByOrgId() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        Integer oldDomainId = SecurityUtil.getPrincipal().getDomainId();
        Integer newDomainId = 100002;
        OrganizationForm form = getOrganizationForm(newDomainId, "increff_2");
        OrganizationData organizationData = organizationDto.add(form);
        organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaVersionId, connectionId));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(newDomainId);
        principal.setRoles(Collections.singletonList(Roles.OMNI_REPORT_STANDARD.getRole()));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
        DashboardData data2 = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_2",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        assertEquals(1, dashboardDto.getDashboardsByOrgId(oldDomainId).size());
        assertEquals(1, dashboardDto.getDashboardsByOrgId(newDomainId).size());

    }

    @Test
    public void testAddDefaultValue() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
    }


    @Test
    public void testDefaultValueForNonExistingParamName() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), "nonExisting", Arrays.asList("1", "2", "3"));
        ApiException apiException = assertThrows(ApiException.class, () -> {
            dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
                });
        assertEquals("Param Name nonExisting does not exist for dashboard", apiException.getMessage());
    }

    @Test
    public void testGetDefaultValueByDashboardId() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());
    }

    //testUpdateUserDefaults
    @Test
    public void testUpdateUserDefaults() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertUserDefaultValues(upsertDefaultValueForm, data.getId());
        dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());
        defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), AbstractDto.getUserId());
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());

        upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("4", "5", "6"));
        defaultValueData = dashboardDto.upsertUserDefaultValues(upsertDefaultValueForm, data.getId());
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(0, defaultValuePojoList.size());
        defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), AbstractDto.getUserId());
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());
    }


    @Test
    public void testUpdateDefaultValue() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());

        upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("4", "5", "6"));
        defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());
    }

    @Test
    public void testDeleteDefaultValue() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));


        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());

        upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), new ArrayList<>());
        defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());

        assertNull(defaultValueData.get(0).getDefaultValue());
        assertNull(defaultValueData.get(0).getDashboardId());
        assertNull(defaultValueData.get(0).getParamName());
    }

    @Test
    public void testDeleteDashboard() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        UpsertDefaultValueForm upsertDefaultValueForm = getUpsertDefaultValueForm(data.getId(), inputControlData.getParamName(), Arrays.asList("1", "2", "3"));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(upsertDefaultValueForm, data.getId(), null);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", upsertDefaultValueForm.getDefaultValueForms().get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardIdUserId(data.getId(), null);
        assertEquals(1, defaultValuePojoList.size());

        List<DashboardChartData> dashboardChartData = dashboardChartDto.getDashboardCharts(data.getId());
        assertEquals(1, dashboardChartData.size());

        dashboardDto.deleteDashboard(data.getId());
        assertEquals(0, dashboardDto.getDashboardsByOrgId().size());
        assertEquals(0, defaultValueApi.getByDashboardIdUserId(data.getId(), null).size());
        assertEquals(0, dashboardChartApi.getByDashboardId(data.getId()).size());
    }

    @Test
    public void testDashboardsWithAllStandardChartsNotVisibleForCustomReportUsers() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 0, 0, RowHeight.HALF))));

        // Create Role Report.Custom User
        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Collections.singletonList(Roles.OMNI_REPORT_CUSTOM.getRole()));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<DashboardPojo> dashboardPojos = dashboardApi.getByOrgId(orgId);
        assertEquals(1, dashboardPojos.size());

        List<DashboardListData> dashboardData = dashboardDto.getDashboardsByOrgId();
        assertEquals(0, dashboardData.size());
    }

    @Test
    public void testValidationGroupMergeMandatory() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);

        // add input control to report
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);

        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());
        reportDto.mapToControl(chartDatas.get(1).getId(), inputControlData.getId());

        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(chartDatas.get(0).getId());
        assertEquals(1, reportControlsPojos.size());

        List<ReportControlsPojo> reportControlsPojos1 = reportControlsApi.getByReportId(chartDatas.get(1).getId());
        assertEquals(1, reportControlsPojos1.size());

        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.MANDATORY, Arrays.asList(inputControlData.getId()));
        reportFlowApi.addValidationGroup(chartDatas.get(1).getId(), groupForm);


        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF))));

        assertEquals(1, data.getFilterDetails().get("common").get(0).getValidationTypes().size());
        assertEquals(ValidationType.MANDATORY, data.getFilterDetails().get("common").get(0).getValidationTypes().get(0));

    }

    @Test
    public void testValidationGroupAddedLater() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);

        // add input control to report
        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);

        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());
        reportDto.mapToControl(chartDatas.get(1).getId(), inputControlData.getId());

        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(chartDatas.get(0).getId());
        assertEquals(1, reportControlsPojos.size());

        List<ReportControlsPojo> reportControlsPojos1 = reportControlsApi.getByReportId(chartDatas.get(1).getId());
        assertEquals(1, reportControlsPojos1.size());

        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF))));

        assertEquals(0, data.getFilterDetails().get("common").get(0).getValidationTypes().size());

        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.MANDATORY, Arrays.asList(inputControlData.getId()));
        reportFlowApi.addValidationGroup(chartDatas.get(1).getId(), groupForm);

        data = dashboardDto.getDashboard(data.getId());
        assertEquals(1, data.getFilterDetails().get("common").get(0).getValidationTypes().size());
        assertEquals(ValidationType.MANDATORY, data.getFilterDetails().get("common").get(0).getValidationTypes().get(0));
    }

    @Test
    public void testValidationGroupMergeDateRange() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);

        // add input control to report
        InputControlForm form = getInputControlForm("Start Date", "startDate", InputControlScope.GLOBAL
                , InputControlType.DATE, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);

        InputControlForm form1 = getInputControlForm("End Date", "endDate", InputControlScope.GLOBAL
                , InputControlType.DATE, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData1 = inputControlDto.add(form1);

        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData1.getId());
        reportDto.mapToControl(chartDatas.get(1).getId(), inputControlData.getId());
        reportDto.mapToControl(chartDatas.get(1).getId(), inputControlData1.getId());

        List<ReportControlsPojo> reportControlsPojos = reportControlsApi.getByReportId(chartDatas.get(0).getId());
        assertEquals(2, reportControlsPojos.size());

        List<ReportControlsPojo> reportControlsPojos1 = reportControlsApi.getByReportId(chartDatas.get(1).getId());
        assertEquals(2, reportControlsPojos1.size());

        ValidationGroupForm groupForm = getValidationGroupForm("group1", 90
                , ValidationType.DATE_RANGE, Arrays.asList(inputControlData.getId(), inputControlData1.getId()));
        reportFlowApi.addValidationGroup(chartDatas.get(1).getId(), groupForm);  //

        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF))));

        assertEquals(1, data.getFilterDetails().get("common").get(0).getValidationTypes().size());
        assertEquals(ValidationType.DATE_RANGE, data.getFilterDetails().get("common").get(0).getValidationTypes().get(0));
    }


    @Test
    public void testSetUserFavoriteDashboard() throws ApiException {
        FavouriteForm favouriteForm = getFavoriteForm(9);
        dashboardDto.setUserFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
    }

    @Test
    public void testSetOrgFavoriteDashboard() throws ApiException {
        FavouriteForm favouriteForm = getFavoriteForm(9);
        dashboardDto.setOrgFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());
    }

    @Test
    public void testUpdateUserFavoriteDashboard() throws ApiException {
        FavouriteForm favouriteForm = getFavoriteForm(9);
        dashboardDto.setUserFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
        favouriteForm = getFavoriteForm(10);
        dashboardDto.setUserFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
    }

    @Test
    public void testUpdateOrgFavoriteDashboard() throws ApiException {
        FavouriteForm favouriteForm = getFavoriteForm(9);
        dashboardDto.setOrgFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());
        favouriteForm = getFavoriteForm(10);
        dashboardDto.setOrgFavoriteDashboard(favouriteForm);
        assertEquals(favouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());
    }

    //testBothFavoriteDashboard
    @Test
    public void testBothFavoriteDashboard() throws ApiException {
        FavouriteForm userFavouriteForm = getFavoriteForm(9);
        FavouriteForm orgFavouriteForm = getFavoriteForm(10);
        dashboardDto.setUserFavoriteDashboard(userFavouriteForm);
        dashboardDto.setOrgFavoriteDashboard(orgFavouriteForm);
        assertEquals(userFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
        assertEquals(orgFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());

        //update user favorite dashboard
        userFavouriteForm = getFavoriteForm(11);
        dashboardDto.setUserFavoriteDashboard(userFavouriteForm);
        assertEquals(userFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
        assertEquals(orgFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());

        //update org favorite dashboard
        orgFavouriteForm = getFavoriteForm(12);
        dashboardDto.setOrgFavoriteDashboard(orgFavouriteForm);
        assertEquals(userFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getUserFav().getFavId());
        assertEquals(orgFavouriteForm.getFavId(), dashboardDto.getFavoriteDashboard().getOrgFav().getFavId());

    }


}
