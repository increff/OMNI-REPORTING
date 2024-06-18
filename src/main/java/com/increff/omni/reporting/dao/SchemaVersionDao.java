package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
@Transactional
public class SchemaVersionDao extends AbstractDao<SchemaVersionPojo> {

    public final static String SELECT_BY_IDS = "SELECT s FROM SchemaVersionPojo s WHERE s.id IN :ids";
    public final static String SELECT_BY_APP_NAMES = "SELECT s FROM SchemaVersionPojo s WHERE s.appName IN :appNames";

    public List<SchemaVersionPojo> selectByAppNames(Set<AppName> appNames) {
        return selectMultiple(createJpqlQuery(SELECT_BY_APP_NAMES).setParameter("appNames", appNames));
    }

    public List<SchemaVersionPojo> selectByIds(List<Integer> ids) {
        return selectMultiple(createJpqlQuery(SELECT_BY_IDS).setParameter("ids", ids));
    }
}
