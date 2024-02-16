package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.increff.omni.reporting.pojo.OrgSchemaVersionPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;

@Repository
public class OrgConnectionDao extends AbstractDao<OrgConnectionPojo> {
    public final String SELECT_BY_ORG_ID_APP_NAME = "select p from OrgConnectionPojo p where p.orgId = :orgId and p.appName = :appName";

    public OrgConnectionPojo getByOrgIdAndAppName(Integer orgId, AppName appName) {
        return selectSingleOrNull(createJpqlQuery(SELECT_BY_ORG_ID_APP_NAME)
                .setParameter("orgId", orgId)
                .setParameter("appName", appName));
    }
}
