package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;

import java.util.Objects;

public class SchemaTestHelper {

    public static SchemaVersionForm getSchemaForm(String name) {
        return getSchemaForm(name, AppName.OMNI);
    }

    public static SchemaVersionForm getSchemaForm(String name, AppName appName) {
        SchemaVersionForm form = new SchemaVersionForm();
        form.setName(name);
        form.setAppName(appName);
        return form;
    }

    public static SchemaVersionPojo getSchemaPojo(String name) {
        return getSchemaPojo(name, AppName.OMNI);
    }

    public static SchemaVersionPojo getSchemaPojo(String name, AppName appName) {
        SchemaVersionPojo pojo = new SchemaVersionPojo();
        pojo.setName(name);
        pojo.setAppName(appName);
        return pojo;
    }
}
