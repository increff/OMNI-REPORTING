package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgConnectionDao;
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

    public OrgConnectionPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgConnectionPojo pojo = getByOrgId(orgId);
        checkNotNull(pojo, "No connection mapped for org : " + orgId);
        return pojo;
    }

    private OrgConnectionPojo getByOrgId(Integer orgId) {
        return dao.select("orgId", orgId);
    }

}
