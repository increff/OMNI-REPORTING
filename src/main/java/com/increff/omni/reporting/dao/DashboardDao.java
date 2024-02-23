package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import java.util.List;


@Repository
@Transactional
public class DashboardDao extends AbstractDao<DashboardPojo> {

    public DashboardPojo getCheck(Integer id) {
        return em().find(DashboardPojo.class, id);
    }

    private static final String SELECT_BY_ORG_ID = "SELECT o FROM DashboardPojo o WHERE o.orgId=:orgId";
    public List<DashboardPojo> getByOrgId(Integer orgId) {
        TypedQuery<DashboardPojo> q = createJpqlQuery(SELECT_BY_ORG_ID);
        q.setParameter("orgId", orgId);
        return selectMultiple(q);
    }

    private static final String SELECT_BY_ORG_ID_NAME = "SELECT o FROM DashboardPojo o WHERE o.orgId=:orgId AND o.name=:name";
    public DashboardPojo getByOrgIdName(Integer orgId, String name) {
        TypedQuery<DashboardPojo> q = createJpqlQuery(SELECT_BY_ORG_ID_NAME);
        q.setParameter("orgId", orgId);
        q.setParameter("name", name);
        return selectSingleOrNull(q);
    }
}
