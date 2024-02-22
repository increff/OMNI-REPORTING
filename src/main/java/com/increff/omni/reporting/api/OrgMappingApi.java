package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgMappingDao;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class OrgMappingApi extends AbstractAuditApi {

    @Autowired
    private OrgMappingDao dao;

    public OrgMappingPojo add(OrgMappingPojo pojo) {
        // todo : add validations
        dao.persist(pojo);
        return pojo;
    }

    public OrgMappingPojo update(Integer id, OrgMappingPojo pojo) throws ApiException {
        OrgMappingPojo existing = getCheck(id);
        existing.setOrgId(pojo.getOrgId());
        existing.setSchemaVersionId(pojo.getSchemaVersionId());
        existing.setConnectionId(pojo.getConnectionId());
        dao.update(pojo);
        return pojo;
    }

    public OrgMappingPojo getCheck(Integer id) throws ApiException {
        OrgMappingPojo pojo = dao.select("id", id);
        checkNotNull(pojo, "OrgMappings : " + id + " does not exist");
        return pojo;
    }

    public List<OrgMappingPojo> selectAll() {
        return dao.selectAll();
    }

    public OrgMappingPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgMappingPojo pojo = getByOrgId(orgId);
        checkNotNull(pojo, "No schema mapped for org : " + orgId);
        return pojo;
    }

    public List<OrgMappingPojo> getBySchemaVersionId(Integer schemaVersionId)  {
        return dao.selectMultiple("schemaVersionId", schemaVersionId);
    }

    private OrgMappingPojo getByOrgId(Integer orgId) {
        return dao.select("orgId", orgId);
    }

    public List<OrgMappingPojo> getCheckBySchemaVersionId(Integer schemaVersionId) throws ApiException {
        List<OrgMappingPojo> pojos = dao.selectMultiple("schemaVersionId", schemaVersionId);
        if(pojos.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "No org mapped to schema version id : " + schemaVersionId);
        return pojos;
    }
}
