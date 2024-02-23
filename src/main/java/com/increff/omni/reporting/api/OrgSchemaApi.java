package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgSchemaDao;
import com.increff.omni.reporting.pojo.OrgSchemaVersionPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class OrgSchemaApi extends AbstractAuditApi {

    @Autowired
    private OrgSchemaDao dao;

    public OrgSchemaVersionPojo map(OrgSchemaVersionPojo pojo) {
        OrgSchemaVersionPojo existing = getByOrgId(pojo.getOrgId());
        if (Objects.isNull(existing)) {
            dao.persist(pojo);
            return pojo;
        } else {
            existing.setSchemaVersionId(pojo.getSchemaVersionId());
            dao.update(existing);
            return existing;
        }
    }

    public List<OrgSchemaVersionPojo> selectAll() {
        return dao.selectAll();
    }

    public OrgSchemaVersionPojo getCheckByOrgId(Integer orgId) throws ApiException {
        OrgSchemaVersionPojo pojo = getByOrgId(orgId);
        checkNotNull(pojo, "No schema mapped for org : " + orgId);
        return pojo;
    }

    public List<OrgSchemaVersionPojo> getBySchemaVersionId(Integer schemaVersionId)  {
        return dao.selectMultiple("schemaVersionId", schemaVersionId);
    }

    private OrgSchemaVersionPojo getByOrgId(Integer orgId) {
        return dao.select("orgId", orgId);
    }

    public List<OrgSchemaVersionPojo> getCheckBySchemaVersionId(Integer schemaVersionId) throws ApiException {
        List<OrgSchemaVersionPojo> pojos = dao.selectMultiple("schemaVersionId", schemaVersionId);
        if(pojos.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "No org mapped to schema version id : " + schemaVersionId);
        return pojos;
    }
}
