package com.increff.omni.reporting.helper;

import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;

public class ConnectionTestHelper {

    public static ConnectionForm getConnectionForm(String host, String name, String username, String password) {
        ConnectionForm form = new ConnectionForm();
        form.setHost(host);
        form.setName(name);
        form.setUsername(username);
        form.setPassword(password);
        return form;
    }

    public static ConnectionPojo getConnectionPojo(String host, String name, String username, String password) {
        ConnectionPojo pojo = new ConnectionPojo();
        pojo.setHost(host);
        pojo.setName(name);
        pojo.setUsername(username);
        pojo.setPassword(password);
        return pojo;
    }
}
