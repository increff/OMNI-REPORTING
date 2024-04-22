package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.constants.DBType;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;

public class ConnectionTestHelper {

    public static ConnectionForm getConnectionForm(String host, String name, String username, String password) {
        return getConnectionForm(host, name, username, password, DBType.MYSQL);
    }

    public static ConnectionForm getConnectionForm(String host, String name, String username, String password, DBType dbType) {
        ConnectionForm form = new ConnectionForm();
        form.setHost(host);
        form.setName(name);
        form.setUsername(username);
        form.setPassword(password);
        form.setDbType(dbType);
        return form;
    }

    public static ConnectionPojo getConnectionPojo(String host, String name, String username, String password) {
        return getConnectionPojo(host, name, username, password, DBType.MYSQL);
    }

    public static ConnectionPojo getConnectionPojo(String host, String name, String username, String password, DBType dbType) {
        ConnectionPojo pojo = new ConnectionPojo();
        pojo.setHost(host);
        pojo.setName(name);
        pojo.setUsername(username);
        pojo.setPassword(password);
        pojo.setDbType(dbType);
        return pojo;
    }
}
