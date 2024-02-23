package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.RowHeight;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.commons.springboot.common.ApiException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.DashboardDtoTestHelper.*;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getChartForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class DashboardChartDtoTest extends AbstractTest {

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
        assertEquals(3, dashboardChartDto.getDashboardCharts(data.getId()).size());
    }

    @Test
    public void testGet() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardChartForm form = getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(form)));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
        List<DashboardChartData> dashboardChartData = dashboardChartDto.getDashboardCharts(data.getId());
        assertEquals(1, dashboardChartData.size());
        assertEquals(form.getChartAlias(), dashboardChartData.get(0).getChartAlias());
        assertEquals(form.getRow(), dashboardChartData.get(0).getRow());
        assertEquals(form.getCol(), dashboardChartData.get(0).getCol());
        assertEquals(form.getColWidth(), dashboardChartData.get(0).getColWidth());
        assertEquals(form.getRowHeight(), dashboardChartData.get(0).getRowHeight());
    }

    @Test
    public void testUpdate() throws ApiException {
        List<ReportData> chartDatas = commonSetup(ReportType.STANDARD);
        DashboardData data = dashboardDto.addDashboard(getDashboardAddForm("Dashboard_1",
                Arrays.asList(getDashboardChartForm(chartDatas.get(0).getAlias(), 0, 0, 0, RowHeight.HALF),
                        getDashboardChartForm(chartDatas.get(1).getAlias(), 1, 0, 0, RowHeight.HALF))));
        assertEquals(data.getId(), dashboardDto.getDashboard(data.getId()).getId());
        assertEquals(2, dashboardChartDto.getDashboardCharts(data.getId()).size());

        DashboardChartForm updatedForm = getDashboardChartForm(chartDatas.get(2).getAlias(), 1, 1, 0, RowHeight.FULL);
        List<DashboardChartData> updatedData = dashboardChartDto.addDashboardChart(Collections.singletonList(updatedForm), data.getId());
        assertEquals(1, updatedData.size());
        assertEquals(updatedForm.getRow(), updatedData.get(0).getRow());
        assertEquals(updatedForm.getCol(), updatedData.get(0).getCol());
        assertEquals(updatedForm.getColWidth(), updatedData.get(0).getColWidth());
        assertEquals(updatedForm.getRowHeight(), updatedData.get(0).getRowHeight());
        assertEquals(updatedForm.getChartAlias(), updatedData.get(0).getChartAlias());
    }



}
