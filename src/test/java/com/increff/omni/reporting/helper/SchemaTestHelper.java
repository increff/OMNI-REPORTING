package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;

public class SchemaTestHelper {

    public static SchemaVersionForm getSchemaForm(String name) {
        SchemaVersionForm form = new SchemaVersionForm();
        form.setName(name);
        return form;
    }

    public static SchemaVersionPojo getSchemaPojo(String name) {
        SchemaVersionPojo pojo = new SchemaVersionPojo();
        pojo.setName(name);
        return pojo;
    }
}
