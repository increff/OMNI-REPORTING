package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportApi extends AbstractAuditApi {

    @Autowired
    private ReportDao dao;

    public ReportPojo add(ReportPojo pojo){
        pojo.setAlias(pojo.getAlias().trim().toLowerCase());
        dao.persist(pojo);
        return pojo;
    }

    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isReport){
        return dao.getByNameAndSchema(name, schemaVersionId, isReport);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isReport, String visualization){
        return dao.getByTypeAndSchema(type, schemaVersionId, isReport, visualization);
    }

    public ReportPojo edit(ReportPojo pojo) throws ApiException {
        ReportPojo existing = getCheck(pojo.getId());
        existing.setDirectoryId(pojo.getDirectoryId());
        existing.setName(pojo.getName());
        existing.setType(pojo.getType());
        existing.setIsEnabled(pojo.getIsEnabled());
        existing.setCanSchedule(pojo.getCanSchedule());
        existing.setIsChart(pojo.getIsChart());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isReport){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIdsAndSchema(ids, schemaVersionId, isReport);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isReport){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIds(ids, isReport);
    }

    public List<ReportPojo> getBySchemaVersion(Integer schemaVersionId, String visualization) {
        return dao.getBySchemaVersionAndTypes(schemaVersionId, visualization);
    }

    public ReportPojo getByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isReport) {
        return dao.getByAliasAndSchema(alias, schemaVersionId, isReport);
    }

    public ReportPojo getCheckByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isReport) throws ApiException {
        ReportPojo pojo = dao.getByAliasAndSchema(alias, schemaVersionId, isReport);
        checkNotNull(pojo, "No report present with alias : " + alias);
        return pojo;
    }
}
