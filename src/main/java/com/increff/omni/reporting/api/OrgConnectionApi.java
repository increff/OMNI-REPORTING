package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgConnectionDao;
import com.increff.omni.reporting.dao.OrgSchemaDao;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrgConnectionApi extends AbstractApi {

    @Autowired
    private OrgConnectionDao dao;

    public OrgConnectionPojo map(OrgConnectionPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public List<OrgConnectionPojo> selectAll(){
        return dao.selectAll();
    }

    public OrgConnectionPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgConnectionPojo pojo = dao.select("orgId", orgId);
        checkNotNull(pojo, "No connection mapped for org : " + orgId);
        return pojo;
    }

}
