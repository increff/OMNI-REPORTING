package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgSchemaDao;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrgSchemaApi extends AbstractApi {

    @Autowired
    private OrgSchemaDao dao;

    public OrgSchemaPojo map(OrgSchemaPojo pojo) {
        OrgSchemaPojo existing = getByOrgId(pojo.getOrgId());
        if (Objects.isNull(existing)) {
            dao.persist(pojo);
            return pojo;
        } else {
            existing.setSchemaId(pojo.getSchemaId());
            dao.update(existing);
            return existing;
        }
    }

    public List<OrgSchemaPojo> selectAll() {
        return dao.selectAll();
    }

    public OrgSchemaPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgSchemaPojo pojo = getByOrgId(orgId);
        checkNotNull(pojo, "No schema mapped for org : " + orgId);
        return pojo;
    }

    private OrgSchemaPojo getByOrgId(Integer orgId) {
        return dao.select("orgId", orgId);
    }


}
