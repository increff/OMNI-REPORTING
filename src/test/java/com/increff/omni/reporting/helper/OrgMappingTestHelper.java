package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.OrgMappingsForm;
import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;

public class OrgMappingTestHelper {

    public static OrgMappingsForm getOrgMappingForm(Integer orgId, Integer schemaVersionId, Integer connectionId) {
        OrgMappingsForm form = new OrgMappingsForm();
        form.setOrgId(orgId);
        form.setSchemaVersionId(schemaVersionId);
        form.setConnectionId(connectionId);
        return form;
    }

    public static OrgMappingPojo getOrgMappingPojo(Integer orgId, Integer schemaVersionId, Integer connectionId) {
        OrgMappingPojo pojo = new OrgMappingPojo();
        pojo.setOrgId(orgId);
        pojo.setSchemaVersionId(schemaVersionId);
        pojo.setConnectionId(connectionId);
        return pojo;
    }

}
