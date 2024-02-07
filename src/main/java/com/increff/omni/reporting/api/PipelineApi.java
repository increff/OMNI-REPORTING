package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.PipelineDao;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class PipelineApi extends AbstractApi {

    @Autowired
    private PipelineDao dao;

    public PipelinePojo add(PipelinePojo pojo) throws ApiException {
        dao.persist(pojo);
        return pojo;
    }

    public PipelinePojo update(Integer id, PipelinePojo pojo) throws ApiException {
        PipelinePojo existing = getCheck(id);
        existing.setConfigs(pojo.getConfigs());
        existing.setName(pojo.getName());
        existing.setOrgId(pojo.getOrgId());
        dao.update(existing);
        return existing;
    }

    public void delete(Integer id) throws ApiException {
        PipelinePojo pojo = getCheck(id);
        dao.remove(pojo);
    }

    public List<PipelinePojo> getByOrgId(Integer orgId) {
        return dao.getByOrgId(orgId);
    }

    private PipelinePojo getCheck(Integer id) throws ApiException {
        PipelinePojo pojo = dao.getCheck(id);
        checkNotNull(pojo, "Pipeline does not exist id: " + id);
        return pojo;
    }

    public PipelinePojo getCheckPipelineOrg(Integer id, Integer orgId) throws ApiException {
        PipelinePojo pojo = getCheck(id);
        if(!Objects.equals(pojo.getOrgId(), orgId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Pipeline " + id + " does not belong to org id: " + orgId);
        }
        return pojo;
    }

    public List<PipelinePojo> getByPipelineIds(List<Integer> pipelineIds) {
        return dao.getByPipelineIds(pipelineIds);
    }
}
