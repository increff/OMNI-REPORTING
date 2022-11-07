package com.increff.omni.reporting.api;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportApi extends AbstractApi {

    @Autowired
    private ReportDao dao;

    public ReportPojo add(ReportPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId){
        return dao.getByNameAndSchema(name, schemaVersionId);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId){
        return dao.getByTypeAndSchema(type, schemaVersionId);
    }

    public ReportPojo edit(ReportPojo pojo) throws ApiException {
        ReportPojo existing = getCheck(pojo.getId());
        existing.setDirectoryId(pojo.getDirectoryId());
        existing.setName(pojo.getName());
        existing.setSchemaVersionId(pojo.getSchemaVersionId());
        existing.setType(pojo.getType());
        existing.setIsEnabled(pojo.getIsEnabled());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIdsAndSchema(ids, schemaVersionId);
    }


    public List<ReportPojo> selectAll() {
        return dao.selectAll();
    }
}
