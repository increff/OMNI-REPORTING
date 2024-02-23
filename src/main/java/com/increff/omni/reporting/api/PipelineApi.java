package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.PipelineDao;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.server.AbstractApi;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.increff.omni.reporting.dto.AbstractDto.getOrgId;

@Log4j2
@Service
@Transactional(rollbackFor = ApiException.class)
public class PipelineApi extends AbstractApi {

    @Autowired
    private PipelineDao dao;

    public PipelinePojo add(PipelinePojo pojo) throws ApiException {
        PipelinePojo existing = dao.getByOrgIdName(pojo.getOrgId(), pojo.getName());
        checkNull(existing, "Pipeline with name " + pojo.getName() + " already exists for this org");

        dao.persist(pojo);
        return pojo;
    }

    public PipelinePojo updateWithUserOrgCheck(Integer id, PipelinePojo pojo) throws ApiException {
        PipelinePojo existing = getCheck(id);
        getCheckPipelineOrg(id, getOrgId());
        if(!Objects.equals(existing.getName(), pojo.getName())) {
            PipelinePojo newNamePojo = dao.getByOrgIdName(pojo.getOrgId(), pojo.getName());
            if(Objects.nonNull(newNamePojo)) {
                log.error("Pipeline with name " + pojo.getName() + " already exists for user's org id: " + pojo.getOrgId());
                throw new ApiException(ApiStatus.BAD_DATA, "Pipeline with name " + pojo.getName() + " already exists for user's org");
            }
        }

        existing.setConfigs(pojo.getConfigs());
        existing.setType(pojo.getType());
        existing.setName(pojo.getName());
        existing.setOrgId(pojo.getOrgId());
        dao.update(existing);
        return existing;
    }

    public List<PipelinePojo> getByOrgId(Integer orgId) {
        return dao.selectMultiple("orgId", orgId);
    }

    public PipelinePojo getCheck(Integer id) throws ApiException {
        PipelinePojo pojo = dao.select(id);
        checkNotNull(pojo, "Pipeline does not exist id: " + id);
        return pojo;
    }

    public PipelinePojo getCheckPipelineOrg(Integer id, Integer orgId) throws ApiException {
        PipelinePojo pojo = getCheck(id);
        if(!Objects.equals(pojo.getOrgId(), orgId)) {
            log.error("Pipeline " + id + " does not belong to org id: " + orgId);
            throw new ApiException(ApiStatus.BAD_DATA, "Pipeline does not belong to this org");
        }
        return pojo;
    }

    public List<PipelinePojo> getByPipelineIds(List<Integer> pipelineIds) {
        return dao.getByIds(pipelineIds);
    }
}
