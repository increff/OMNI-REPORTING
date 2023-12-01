package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.ApplicationProperties;
import com.increff.omni.reporting.model.constants.*;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
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
import static org.junit.Assert.*;

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
    private ApplicationProperties properties;


    Integer schemaVersionId;
    private List<ReportData> commonSetup( ReportType type) throws ApiException {
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
        schemaVersionId = schemaData.getId();
        ConnectionForm connectionForm = getConnectionForm("127.0.0.1", "Test DB", username, password);
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.mapToConnection(organizationData.getId(), connectionData.getId());
        organizationDto.mapToSchema(organizationData.getId(), schemaData.getId());
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

    @Test(expected = ApiException.class)
    public void testExceedDashboardsPerOrgMaxLimit() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        for(int i = 0; i<properties.getMaxDashboardsPerOrg() + 1; i++) {
        	DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_"+i,
                    Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        }
    }

    @Test
    public void testUpdate() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1", Collections.singletonList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        DashboardForm form = getDashboardForm("Dashboard_2");
        dashboardDto.updateDashboard(form ,data.getId());
        assertEquals(dashboardDto.getDashboardsByOrgId().get(0).getName(), "Dashboard_2");
    }

    @Test(expected = ApiException.class)
    public void testSameChartTwiceInDashboardError() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(0).getAlias(), 1, 0, 0, RowHeight.HALF))));

    }

    @Test
    public void testGet() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
    }

    @Test(expected = ApiException.class)
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
        dashboardDto.getDashboard(data.getId());
    }

//    @Test
//    public void testGetByOrgId() throws ApiException {
//        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
//        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
//                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
//
//        Integer oldDomainId = SecurityUtil.getPrincipal().getDomainId();
//        Integer newDomainId = 100002;
//        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
//        Authentication authentication = Mockito.mock(Authentication.class);
//        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
//        UserPrincipal principal = new UserPrincipal();
//        principal.setDomainId(newDomainId);
//        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
//        SecurityContextHolder.setContext(securityContext);
//        DashboardData data2 = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_2",
//                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));
// // No schema mapped for org : 100002
//        assertEquals(1, dashboardDto.getDashboardsByOrgId(oldDomainId).size());
//        assertEquals(1, dashboardDto.getDashboardsByOrgId(newDomainId).size());
//
//    }

    @Test
    public void testAddDefaultValue() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        List<DefaultValueForm> defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("1", "2", "3")));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
    }

    @Test(expected = ApiException.class)
    public void testDefaultValueForNonExistingControlId() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        List<DefaultValueForm> defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), -1, Arrays.asList("1", "2", "3")));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
    }

    // defaultValueApi.getByDashboardId
    @Test
    public void testGetDefaultValueByDashboardId() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF))));

        InputControlForm form = getInputControlForm("Client Id", "clientId", InputControlScope.GLOBAL
                , InputControlType.TEXT, new ArrayList<>(), null, null, schemaVersionId);
        InputControlData inputControlData = inputControlDto.add(form);
        reportDto.mapToControl(chartDatas.get(0).getId(), inputControlData.getId());

        List<DefaultValueForm> defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("1", "2", "3")));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardId(data.getId());
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());
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

        List<DefaultValueForm> defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("1", "2", "3")));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardId(data.getId());
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());

        defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("4", "5", "6")));
        defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        defaultValuePojoList = defaultValueApi.getByDashboardId(data.getId());
        assertEquals(1, defaultValuePojoList.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());
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

    	List<DefaultValueForm> defaultValueForms = new ArrayList<>();
    	defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("1", "2", "3")));
    	List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
    	assertEquals(1, defaultValueData.size());
    	assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
    	List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardId(data.getId());
    	assertEquals(1, defaultValuePojoList.size());
    	assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValuePojoList.get(0).getDefaultValue());

        defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), new ArrayList<>()));
        defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());

        assertNull(defaultValueData.get(0).getDefaultValue());
        assertNull(defaultValueData.get(0).getDashboardId());
        assertNull(defaultValueData.get(0).getControlId());
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

        List<DefaultValueForm> defaultValueForms = new ArrayList<>();
        defaultValueForms.add(getDefaultValueForm(data.getId(), inputControlData.getId(), Arrays.asList("1", "2", "3")));
        List<DefaultValueData> defaultValueData = dashboardDto.upsertDefaultValues(defaultValueForms);
        assertEquals(1, defaultValueData.size());
        assertEquals(String.join(",", defaultValueForms.get(0).getDefaultValue()), defaultValueData.get(0).getDefaultValue());
        List<DefaultValuePojo> defaultValuePojoList = defaultValueApi.getByDashboardId(data.getId());
        assertEquals(1, defaultValuePojoList.size());

        List<DashboardChartData> dashboardChartData = dashboardChartDto.getDashboardCharts(data.getId());
        assertEquals(1, dashboardChartData.size());

        dashboardDto.deleteDashboard(data.getId());
        assertEquals(0, dashboardDto.getDashboardsByOrgId().size());
        assertEquals(0, defaultValueApi.getByDashboardId(data.getId()).size());
        assertEquals(0, dashboardChartApi.getByDashboardId(data.getId()).size());
    }
}
