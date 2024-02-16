package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgConnectionDao;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class OrgConnectionApi extends AbstractAuditApi {

    @Autowired
    private OrgConnectionDao dao;

    public OrgConnectionPojo map(OrgConnectionPojo pojo){
        OrgConnectionPojo existing = getByOrgId(pojo.getOrgId());
        if (Objects.isNull(existing)) {
            dao.persist(pojo);
            return pojo;
        } else {
            existing.setConnectionId(pojo.getConnectionId());
            dao.update(existing);
            return existing;
        }
    }

    public List<OrgConnectionPojo> selectAll(){
        return dao.selectAll();
    }

    public OrgConnectionPojo getCheckByOrgIdAppName(Integer orgId, AppName appName) throws ApiException {
        OrgConnectionPojo pojo = getByOrgIdAppName(orgId, appName);
        checkNotNull(pojo, "No connection mapped for org : " + orgId + " and app : " + appName);
        return pojo;
    }

    private OrgConnectionPojo getByOrgIdAppName(Integer orgId, AppName appName) {
        return dao.getByOrgIdAndAppName(orgId, appName);
    }

    private OrgConnectionPojo getByOrgId(Integer orgId) {
        return dao.select("orgId", orgId);
    }

}
