package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.SchemaForm;
import com.increff.omni.reporting.pojo.SchemaPojo;

public class SchemaTestHelper {

    public static SchemaForm getSchemaForm(String name) {
        SchemaForm form = new SchemaForm();
        form.setName(name);
        return form;
    }

    public static SchemaPojo getSchemaPojo(String name) {
        SchemaPojo pojo = new SchemaPojo();
        pojo.setName(name);
        return pojo;
    }
}
