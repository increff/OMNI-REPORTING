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
import java.util.Locale;

@Service
@Transactional(rollbackFor = Exception.class)
public class ReportApi extends AbstractAuditApi {

    @Autowired
    private ReportDao dao;

    public ReportPojo add(ReportPojo pojo){
        pojo.setAlias(pojo.getAlias().trim().toUpperCase(Locale.ROOT));
        dao.persist(pojo);
        return pojo;
    }

    public ReportPojo getCheck(Integer id) throws ApiException {
        ReportPojo pojo = dao.select(id);
        checkNotNull(pojo, "No report present with id : " + id);
        return pojo;
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isDashboard){
        return dao.getByNameAndSchema(name, schemaVersionId, isDashboard);
    }

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isDashboard){
        return dao.getByTypeAndSchema(type, schemaVersionId, isDashboard);
    }

    public ReportPojo edit(ReportPojo pojo) throws ApiException {
        ReportPojo existing = getCheck(pojo.getId());
        existing.setDirectoryId(pojo.getDirectoryId());
        existing.setName(pojo.getName());
        existing.setType(pojo.getType());
        existing.setIsEnabled(pojo.getIsEnabled());
        existing.setCanSchedule(pojo.getCanSchedule());
        existing.setIsDashboard(pojo.getIsDashboard());
        dao.update(existing);
        return existing;
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isDashboard){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIdsAndSchema(ids, schemaVersionId, isDashboard);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isDashboard){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.getByIds(ids, isDashboard);
    }

    public List<ReportPojo> getBySchemaVersion(Integer schemaVersionId) {
        return dao.selectMultiple("schemaVersionId", schemaVersionId);
    }

    public ReportPojo getByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isDashboard) {
        return dao.getByAliasAndSchema(alias, schemaVersionId, isDashboard);
    }
}
