package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgSchemaDao;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class OrgSchemaApi extends AbstractApi {

    @Autowired
    private OrgSchemaDao dao;

    public OrgSchemaPojo map(OrgSchemaPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public List<OrgSchemaPojo> selectAll(){
        return dao.selectAll();
    }

    public OrgSchemaPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgSchemaPojo pojo = dao.select("orgId", orgId);
        checkNotNull(pojo, "No schema mapped for org : " + orgId);
        return pojo;
    }


}
