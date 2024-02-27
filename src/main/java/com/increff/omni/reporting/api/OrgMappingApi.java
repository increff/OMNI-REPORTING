package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrgMappingDao;
import com.increff.omni.reporting.pojo.OrgMappingPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Log4j
@Service
@Transactional(rollbackFor = ApiException.class)
public class OrgMappingApi extends AbstractAuditApi {

    @Autowired
    private OrgMappingDao dao;

    public OrgMappingPojo add(OrgMappingPojo pojo) throws ApiException {
        OrgMappingPojo existing = getByOrgIdSchemaVersionId(pojo.getOrgId(), pojo.getSchemaVersionId());
        if(Objects.nonNull(existing)){
            log.error("OrgMapping already exists for org : " + pojo.getOrgId() + " and schema version id : " + pojo.getSchemaVersionId());
            throw new ApiException(ApiStatus.BAD_DATA, "OrgMapping already exists for org and schema version id");
        }
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

    public List<OrgMappingPojo> getCheckByOrgId(Integer orgId) throws ApiException {
        List<OrgMappingPojo> pojos = getByOrgId(orgId);
        if(pojos.isEmpty()){
            log.error("No mappings for org : " + orgId);
            throw new ApiException(ApiStatus.BAD_DATA, "No mappings for org");
        }
        return pojos;
    }

    public OrgMappingPojo getCheckByOrgIdSchemaVersionId(Integer orgId, Integer schemaVersionId) throws ApiException {
        OrgMappingPojo pojo = getByOrgIdSchemaVersionId(orgId, schemaVersionId);
        if(pojo == null){
            log.error("No mappings for org : " + orgId + " and schema version id : " + schemaVersionId);
            throw new ApiException(ApiStatus.BAD_DATA, "No mappings for org and schema version id");
        }
        return pojo;
    }

    public OrgMappingPojo getByOrgIdSchemaVersionId(Integer orgId, Integer schemaVersionId) {
        return dao.selectByOrgIdSchemaVersionId(orgId, schemaVersionId);
    }


    public List<OrgMappingPojo> getBySchemaVersionId(Integer schemaVersionId)  {
        return dao.selectMultiple("schemaVersionId", schemaVersionId);
    }

    private List<OrgMappingPojo> getByOrgId(Integer orgId) {
        return dao.selectMultiple("orgId", orgId);
    }

    public List<OrgMappingPojo> getCheckBySchemaVersionId(Integer schemaVersionId) throws ApiException {
        List<OrgMappingPojo> pojos = dao.selectMultiple("schemaVersionId", schemaVersionId);
        if(pojos.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "No org mapped to schema version id : " + schemaVersionId);
        return pojos;
    }
}
