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
@Transactional(rollbackFor = ApiException.class)
public class ReportApi extends AbstractApi {

    @Autowired
    private ReportDao dao;

    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }

    public ReportPojo getByName(String name){
        return dao.select("name",name);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaId){
        return dao.getByTypeAndSchema(type, schemaId);
    }

    public ReportPojo getByTypeAndName(ReportType type, String name){
        return dao.getByTypeAndName(type, name);
    }

    public ReportPojo add(ReportPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public ReportPojo edit(ReportPojo pojo) throws ApiException {
        ReportPojo existing = getCheck(pojo.getId());
        existing.setDirectoryId(pojo.getDirectoryId());
        existing.setName(pojo.getName());
        existing.setSchemaId(pojo.getSchemaId());
        existing.setType(pojo.getType());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaId){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIdsAndSchema(ids, schemaId);
    }



}
