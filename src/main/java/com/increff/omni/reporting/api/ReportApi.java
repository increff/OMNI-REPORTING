package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ReportDao;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.util.UserPrincipalUtil.NULL_SCHEMA_VERSION_APPS;

@Log4j
@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportApi extends AbstractAuditApi {

    @Autowired
    private ReportDao dao;

    public ReportPojo add(ReportPojo pojo) throws ApiException{
        pojo.setAlias(pojo.getAlias().trim().toLowerCase());

        ReportPojo existing = getByAliasSchemaIsChartAppName(pojo.getAlias(), pojo.getSchemaVersionId(), pojo.getIsChart(), pojo.getAppName());
        if(Objects.nonNull(existing)) {
            log.error("Report already present alias : " + pojo.getAlias() + " schemaVersionId : " + pojo.getSchemaVersionId() + " isChart : " + pojo.getIsChart() + " appName : " + pojo.getAppName());
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with isChart, alias : " + pojo.getAlias() + " isChart : " + pojo.getIsChart() + " appName : " + pojo.getAppName());
        }
        dao.persist(pojo);
        return pojo;
    }

    public ReportPojo getByAliasSchemaIsChartAppName(String alias, Integer schemaVersionId, Boolean isChart, AppName appName){
        return dao.getByAliasSchemaIsChartAppName(alias, schemaVersionId, isChart, appName);
    }

    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }

    public ReportPojo getCheckAppAccess(Integer id, Set<AppName> appNames) throws ApiException {
        ReportPojo pojo = getCheck(id);
        if(!appNames.contains(pojo.getAppName()))
            throw new ApiException(ApiStatus.AUTH_ERROR, "Report Forbidden for App Name : " + pojo.getAppName());
        return pojo;
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isChart){
        return dao.getByNameAndSchema(name, schemaVersionId, isChart);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isChart,
                                               VisualizationType visualization, Set<AppName> appName){
        List<ReportPojo> reports = dao.getByTypeAndSchema(type, schemaVersionId, isChart, visualization, appName);
        appName = appName.stream().filter(NULL_SCHEMA_VERSION_APPS::contains).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(appName))
            reports.addAll(dao.getByTypeAndSchema(type, null, isChart, visualization, appName));
        return reports;
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
        existing.setAppName(pojo.getAppName());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isChart, Set<AppName> appNames){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        List<ReportPojo> reports = dao.getByIdsAndSchema(ids, schemaVersionId, isChart, appNames);
        appNames = appNames.stream().filter(NULL_SCHEMA_VERSION_APPS::contains).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(appNames))
            reports.addAll(dao.getByIdsAndSchema(ids, null, isChart, appNames));
        return reports;
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isChart){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIds(ids, isChart);
    }

    public List<ReportPojo> getBySchemaVersion(Integer schemaVersionId, Set<AppName> appNames, VisualizationType visualization) {
        return dao.getBySchemaVersionAppNameAndTypes(schemaVersionId, appNames, visualization);
    }

    public ReportPojo getByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isChart) throws ApiException {
        if(NULL_SCHEMA_VERSION_APPS.contains(checkAliasAppName(alias)))
            schemaVersionId = null;

        return dao.getByAliasAndSchema(alias, schemaVersionId, isChart);
    }

    public ReportPojo getCheckByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isChart) throws ApiException {
        ReportPojo pojo = getByAliasAndSchema(alias, schemaVersionId, isChart);
        checkNotNull(pojo, "No report present with alias : " + alias + " schemaVersionId : " + schemaVersionId + " isChart : " + isChart);
        return pojo;
    }

    public List<ReportPojo> getByAliasAndSchema(List<String> aliasList, Integer schemaVersionId, Boolean isChart) throws ApiException {
        List<String> nullSchemaAliases = new ArrayList<>();
        for(String alias : aliasList){
            if(NULL_SCHEMA_VERSION_APPS.contains(checkAliasAppName(alias)))
                nullSchemaAliases.add(alias);
        }
        aliasList.removeAll(nullSchemaAliases);
        List<ReportPojo> reports = new ArrayList<>();
        if(!CollectionUtils.isEmpty(aliasList))
            reports.addAll(dao.getByAliasAndSchema(aliasList, schemaVersionId, isChart));
        if(!CollectionUtils.isEmpty(nullSchemaAliases))
            reports.addAll(dao.getByAliasAndSchema(nullSchemaAliases, null, isChart));
        return reports;
    }

    private AppName checkAliasAppName(String alias) throws ApiException {
        // todo : explore if this can be moved to rule engine
        List<ReportPojo> reports = dao.selectMultiple("alias", alias);

        Set<AppName> appNames = reports.stream().map(ReportPojo::getAppName).collect(Collectors.toSet());
        if(appNames.size() > 1)
            throw new ApiException(ApiStatus.BAD_DATA, "Multiple app names found for alias : " + alias);
        AppName appName = appNames.iterator().next();

        if(reports.size() > 1 && NULL_SCHEMA_VERSION_APPS.contains(appName))
            throw new ApiException(ApiStatus.BAD_DATA, "Multiple reports found for null schema version app alias : " + alias + " app name : " + appName);

        return appName;
    }
}
