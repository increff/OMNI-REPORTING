package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j
@Service
@Transactional(rollbackFor = ApiException.class)
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

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isChart){
        return dao.getByNameAndSchema(name, schemaVersionId, isChart);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, List<Integer> schemaVersionIds, Boolean isChart, VisualizationType visualization){
        return dao.getByTypeAndSchema(type, schemaVersionIds, isChart, visualization);
    }

    public ReportPojo edit(ReportPojo pojo) throws ApiException {
        ReportPojo existing = getCheck(pojo.getId());
        existing.setDirectoryId(pojo.getDirectoryId());
        existing.setName(pojo.getName());
        existing.setType(pojo.getType());
        existing.setIsEnabled(pojo.getIsEnabled());
        existing.setCanSchedule(pojo.getCanSchedule());
        existing.setMinFrequencyAllowedSeconds(pojo.getMinFrequencyAllowedSeconds());
        existing.setIsChart(pojo.getIsChart());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, List<Integer> schemaVersionIds, Boolean isChart){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIdsAndSchema(ids, schemaVersionIds, isChart);
    }

    public List<ReportPojo> getByIds(List<Integer> ids){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIds(ids);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isChart){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIds(ids, isChart);
    }

    public List<ReportPojo> getBySchemaVersion(Integer schemaVersionId, VisualizationType visualization) {
        return dao.getBySchemaVersionAndTypes(schemaVersionId, visualization);
    }

    public ReportPojo getByAliasAndSchema(String alias, List<Integer> schemaVersionIds, Boolean isChart) {
        return dao.getByAliasAndSchema(alias, schemaVersionIds, isChart);
    }

    public ReportPojo getCheckByAliasAndSchema(String alias, List<Integer> schemaVersionId, Boolean isChart) throws ApiException {
        ReportPojo pojo = dao.getByAliasAndSchema(alias, schemaVersionId, isChart);
        checkNotNull(pojo, "No report present with alias : " + alias + " schemaVersionId : " + schemaVersionId + " isChart : " + isChart);
        return pojo;
    }

    public List<ReportPojo> getByAliasAndSchema(List<String> aliasList, List<Integer> schemaVersionIds, Boolean isChart) {
        return dao.getByAliasAndSchema(aliasList, schemaVersionIds, isChart);
    }

    public List<ReportPojo> getByAlias(String alias){
        return dao.selectMultiple("alias", alias);
    }

    public ReportPojo getByIdAndAppNameIn(Integer id, Set<AppName> appNames) {
        return dao.getByIdAndAppNameIn(id, appNames);
    }
}
