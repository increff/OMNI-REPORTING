package com.increff.omni.reporting.util;

import org.apache.commons.dbcp2.BasicDataSource;

public class DbPoolUtil {
    public DbPoolUtil() {
    }

    public static BasicDataSource initDataSource(String driverClassName, String url, String userName, String password, int minConnection, int maxConnection) {
        BasicDataSource bean = new BasicDataSource();
        bean.setDriverClassName(driverClassName);
        bean.setUrl(url);
        bean.setUsername(userName);
        bean.setPassword(password);
        bean.setInitialSize(minConnection);
        bean.setMaxTotal(maxConnection);
        bean.setMaxIdle(minConnection);
        bean.setValidationQuery("SELECT 1");
        return bean;
    }
}
