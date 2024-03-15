package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dto.*;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.RowHeight;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DashboardDtoTestHelper.getDashboardAddForm;
import static com.increff.omni.reporting.helper.DashboardDtoTestHelper.getDashboardChartForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getChartForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.assertEquals;

public class DashboardChartApiTest extends AbstractTest {

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
        ConnectionForm connectionForm = getConnectionForm("127.0.0.1", "Test DB", username, password);
        ConnectionData connectionData = connectionDto.add(connectionForm);
        organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaData.getId(), connectionData.getId()));
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
    public void testGetCheckByDashboardAndChartAlias() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
        assertEquals(3, dashboardChartDto.getDashboardCharts(data.getId()).size());
        DashboardChartPojo pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(0).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(0).getAlias());
        pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(1).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(1).getAlias());
        pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(2).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(2).getAlias());
    }

    @Test(expected = ApiException.class)
    public void testGetCheckByDashboardAndChartIncorrectAlias() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
        assertEquals(3, dashboardChartDto.getDashboardCharts(data.getId()).size());
        DashboardChartPojo pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(0).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(0).getAlias());
        pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(1).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(1).getAlias());
        pojo = dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), chartDatas.get(2).getAlias());
        assertEquals(pojo.getDashboardId(), data.getId());
        assertEquals(pojo.getChartAlias(), chartDatas.get(2).getAlias());
        dashboardChartApi.getCheckByDashboardAndChartAlias(data.getId(), "Incorrect");
    }

    @Test
    public void testDeleteByDashboardId() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 0, 1, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
        assertEquals(3, dashboardChartDto.getDashboardCharts(data.getId()).size());
        dashboardChartApi.deleteByDashboardId(data.getId());
        assertEquals(0, dashboardChartDto.getDashboardCharts(data.getId()).size());
    }





}
