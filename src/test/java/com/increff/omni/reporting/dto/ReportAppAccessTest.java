package com.increff.omni.reporting.dto;

import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.flow.ReportFlowApi;
import com.increff.omni.reporting.helper.OrgMappingTestHelper;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.Roles;
import com.increff.omni.reporting.model.data.*;
import com.increff.omni.reporting.model.form.*;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.increff.omni.reporting.helper.ConnectionTestHelper.getConnectionForm;
import static com.increff.omni.reporting.helper.CustomReportAccessTestHelper.getCustomReportAccessForm;
import static com.increff.omni.reporting.helper.DirectoryTestHelper.getDirectoryForm;
import static com.increff.omni.reporting.helper.OrgMappingTestHelper.getOrgMappingForm;
import static com.increff.omni.reporting.helper.OrgTestHelper.getOrganizationForm;
import static com.increff.omni.reporting.helper.ReportTestHelper.getReportForm;
import static com.increff.omni.reporting.helper.SchemaTestHelper.getSchemaForm;
import static org.junit.Assert.assertEquals;

public class ReportAppAccessTest extends AbstractTest {

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
    @Autowired
    private ReportFlowApi reportFlowApi;

    private final Integer orgId = 100001;

    private Integer v1SchemaId;
    private Integer v2SchemaId;
    private Integer u1SchemaId;
    private Integer u2SchemaId;

    private Integer omniOrgMappingId;
    private Integer unifyOrgMappingId;

    private Integer connectionId;


    private void commonSetup() throws ApiException {
        reportDto.setEncryptionClient(encryptionClient);
        inputControlDto.setEncryptionClient(encryptionClient);
        connectionDto.setEncryptionClient(encryptionClient);
        OrganizationForm form = getOrganizationForm(orgId, "increff");
        OrganizationData organizationData = organizationDto.add(form);
        List<DirectoryData> data = directoryDto.getAllDirectories();
        DirectoryForm directoryForm = getDirectoryForm("Standard Reports", data.get(0).getId());
        DirectoryData directoryData = directoryDto.add(directoryForm);

        ConnectionForm connectionForm = getConnectionForm("dev-db.increff.com", "Dev DB", "db.user", "db.password");
        ConnectionData connectionData = connectionDto.add(connectionForm);
        connectionId = connectionData.getId();

        ReportForm reportForm;
        ReportData reportData;
        SchemaVersionForm schemaVersionForm;
        SchemaVersionData schemaData;
        CustomReportAccessForm customReportAccessForm;
        OrgMappingsData orgMappingsData;

        // 8 reports (2 standard and 2 custom for 2 schema versions)
        schemaVersionForm = getSchemaForm("V1", AppName.OMNI);
        schemaData = schemaDto.add(schemaVersionForm);
        v1SchemaId = schemaData.getId();
        reportForm = getReportForm("V1 Standard", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        reportForm = getReportForm("V1 Custom", ReportType.CUSTOM, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        customReportAccessForm = getCustomReportAccessForm(reportData.getId(), orgId);
        dto.addCustomReportAccess(customReportAccessForm);

        orgMappingsData = organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaData.getId(), connectionId));
        omniOrgMappingId = orgMappingsData.getId();

        schemaVersionForm = getSchemaForm("V2", AppName.OMNI);
        schemaData = schemaDto.add(schemaVersionForm);
        v2SchemaId = schemaData.getId();
        reportForm = getReportForm("V2 Standard", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        reportForm = getReportForm("V2 Custom", ReportType.CUSTOM, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        customReportAccessForm = getCustomReportAccessForm(reportData.getId(), orgId);
        dto.addCustomReportAccess(customReportAccessForm);

        schemaVersionForm = getSchemaForm("U1", AppName.IIP);
        schemaData = schemaDto.add(schemaVersionForm);
        u1SchemaId = schemaData.getId();
        reportForm = getReportForm("U1 Standard", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        reportForm = getReportForm("U1 Custom", ReportType.CUSTOM, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        customReportAccessForm = getCustomReportAccessForm(reportData.getId(), orgId);
        dto.addCustomReportAccess(customReportAccessForm);

        orgMappingsData = organizationDto.addOrgMapping(OrgMappingTestHelper.getOrgMappingForm(organizationData.getId(), schemaData.getId(), connectionId));
        unifyOrgMappingId = orgMappingsData.getId();

        schemaVersionForm = getSchemaForm("U2", AppName.IIP);
        schemaData = schemaDto.add(schemaVersionForm);
        u2SchemaId = schemaData.getId();
        reportForm = getReportForm("U2 Standard", ReportType.STANDARD, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        reportForm = getReportForm("U2 Custom", ReportType.CUSTOM, directoryData.getId(), schemaData.getId(), false, ChartType.REPORT);
        reportData = reportDto.add(reportForm);
        customReportAccessForm = getCustomReportAccessForm(reportData.getId(), orgId);
        dto.addCustomReportAccess(customReportAccessForm);
    }

    @Test
    public void testAccessOmniReportStandard() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Collections.singletonList(Roles.OMNI_REPORT_STANDARD.getRole()));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(2, reports.size());

        assertEquals(v1SchemaId, reports.get(0).getSchemaVersionId());
        assertEquals(v1SchemaId, reports.get(1).getSchemaVersionId());

        assertEquals(1, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(1, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }

    @Test
    public void testAccessOmniReportCustom() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Collections.singletonList(Roles.OMNI_REPORT_CUSTOM.getRole()));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(1, reports.size());

        assertEquals(v1SchemaId, reports.get(0).getSchemaVersionId());

        assertEquals(1, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }

    //testAccessOmniStandardUnifyStandard
    @Test
    public void testAccessOmniStandardUnifyStandard() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Arrays.asList(Roles.OMNI_REPORT_STANDARD.getRole(), "iip.report.standard"));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(4, reports.size());

        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(v1SchemaId)).count());
        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(u1SchemaId)).count());

        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }

    @Test
    public void testAccessOmniStandardUnifyCustom() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Arrays.asList(Roles.OMNI_REPORT_STANDARD.getRole(), "iip.report.custom"));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(3, reports.size());

        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(v1SchemaId)).count());
        assertEquals(1, reports.stream().filter(report -> report.getSchemaVersionId().equals(u1SchemaId)).count());

        assertEquals(1, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }

    @Test
    public void testAccessOmniCustomUnifyStandard() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Arrays.asList(Roles.OMNI_REPORT_CUSTOM.getRole(), "iip.report.standard"));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(3, reports.size());

        assertEquals(1, reports.stream().filter(report -> report.getSchemaVersionId().equals(v1SchemaId)).count());
        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(u1SchemaId)).count());

        assertEquals(1, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }

    //testAccessOmniStandardUnifyStandardChangeOrgMapping
    @Test
    public void testAccessOmniStandardUnifyStandardChangeOrgMapping() throws ApiException {
        commonSetup();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setRoles(Arrays.asList(Roles.OMNI_REPORT_STANDARD.getRole(), "iip.report.standard"));
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        List<ReportPojo> reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(4, reports.size());

        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(v1SchemaId)).count());
        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(u1SchemaId)).count());

        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());

        // Change org mapping
        OrgMappingsForm orgMappingForm = getOrgMappingForm(orgId, v2SchemaId, connectionId);
        organizationDto.editOrgMappings(omniOrgMappingId, orgMappingForm);

        orgMappingForm = getOrgMappingForm(orgId, u2SchemaId, connectionId);
        organizationDto.editOrgMappings(unifyOrgMappingId, orgMappingForm);

        // Refresh
        reports = reportFlowApi.getAll(orgId, false, null);

        assertEquals(4, reports.size());

        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(v2SchemaId)).count());
        assertEquals(2, reports.stream().filter(report -> report.getSchemaVersionId().equals(u2SchemaId)).count());

        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.STANDARD)).count());
        assertEquals(2, reports.stream().filter(report -> report.getType().equals(ReportType.CUSTOM)).count());
    }
}
