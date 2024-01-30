package com.increff.omni.reporting.dto;

import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.CustomReportAccessTestHelper.getCustomReportAccessForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static com.increff.omni.reporting.security.StandardSecurityConfig.REPORT_CUSTOM;
import static org.junit.Assert.assertEquals;

public class CustomReportAccessDtoTest extends AbstractTest {

    @Autowired
    private CustomReportAccessDto dto;
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

    private final Integer orgId = 100001;

    private ReportForm commonSetup(String name, ReportType type) throws ApiException {
        reportDto.setEncryptionClient(encryptionClient);
        inputControlDto.setEncryptionClient(encryptionClient);
        connectionDto.setEncryptionClient(encryptionClient);
        OrganizationForm form = getOrganizationForm(orgId, "increff");
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
        return getReportForm(name, type, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
    }

    @Test
    public void testCustomAccess() throws ApiException {
        ReportForm reportForm = commonSetup("Report 1", ReportType.CUSTOM);
        ReportData reportData = reportDto.add(reportForm);
        CustomReportAccessForm form = getCustomReportAccessForm(reportData.getId(), 100001);
        dto.addCustomReportAccess(form);
        List<CustomReportAccessData> dataList = dto.getAllDataByReport(reportData.getId());
        assertEquals(1, dataList.size());
        assertEquals(reportData.getName(), dataList.get(0).getReportName());
        assertEquals(100001, dataList.get(0).getOrgId().intValue());
        dto.deleteCustomReportAccess(dataList.get(0).getId());
        dataList = dto.getAllDataByReport(reportData.getId());
        assertEquals(0, dataList.size());
    }

    @Test(expected = ApiException.class)
    public void testCustomAccessForStandardReport() throws ApiException {
        ReportForm reportForm = commonSetup("Report 2", ReportType.STANDARD);
        ReportData reportData = reportDto.add(reportForm);
        CustomReportAccessForm form = getCustomReportAccessForm(reportData.getId(), 100001);
        try {
            dto.addCustomReportAccess(form);
        } catch (ApiException e) {
            assertEquals(ApiStatus.BAD_DATA, e.getStatus());
            assertEquals("Report type is STANDARD, custom access is not required here.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCustomReportRoleUserAccess() throws ApiException {
        ReportForm reportForm = commonSetup("Report 1", ReportType.CUSTOM);
        ReportData reportData = reportDto.add(reportForm);
        CustomReportAccessForm form = getCustomReportAccessForm(reportData.getId(), orgId);
        dto.addCustomReportAccess(form);

        ReportForm standardReportForm = getReportForm("Report 3", ReportType.STANDARD, reportForm.getDirectoryId(), reportForm.getSchemaVersionId(), false, ChartType.REPORT);
        reportDto.add(standardReportForm);

        List<ReportData> allReports = reportDto.selectAllBySchemaVersion(reportForm.getSchemaVersionId(), null);
        assertEquals(2, allReports.size());

        // Create Role Report.Custom User
        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Collections.singletonList(REPORT_CUSTOM));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportData> reports = reportDto.selectByOrg(orgId, false, null);
        assertEquals(1, reports.size());
        assertEquals(ReportType.CUSTOM, reports.get(0).getType());
        assertEquals(reportData.getId(), reports.get(0).getId());
        assertEquals(reportData.getName(), reports.get(0).getName());
    }
}
