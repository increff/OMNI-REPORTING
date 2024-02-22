package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.OrganizationForm;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.increff.omni.reporting.pojo.OrganizationPojo;

public class OrgTestHelper {

    public static OrganizationForm getOrganizationForm(Integer orgId, String orgName) {
        OrganizationForm organizationForm = new OrganizationForm();
        organizationForm.setId(orgId);
        organizationForm.setName(orgName);
        return organizationForm;
    }

    public static OrganizationPojo getOrgPojo(Integer orgId, String orgName) {
        OrganizationPojo pojo = new OrganizationPojo();
        pojo.setId(orgId);
        pojo.setName(orgName);
        return pojo;
    }

    public static OrgMappingPojo getOrgSchemaPojo(int orgId, int schemaVersionId) {
        OrgMappingPojo pojo = new OrgMappingPojo();
        pojo.setSchemaVersionId(schemaVersionId);
        pojo.setOrgId(orgId);
        return pojo;
    }

    public static OrgConnectionPojo getOrgConnectionPojo(Integer orgId, Integer connectionId) {
        OrgConnectionPojo pojo = new OrgConnectionPojo();
        pojo.setConnectionId(connectionId);
        pojo.setOrgId(orgId);
        return pojo;
    }
}
