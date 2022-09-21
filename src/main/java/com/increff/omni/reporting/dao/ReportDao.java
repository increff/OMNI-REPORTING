package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class ReportDao extends AbstractDao<ReportPojo> {

    private static final String selectByTypeAndSchema = "SELECT r FROM ReportPojo r" //
            + " WHERE r.type = :type and r.schemaId = :id";

    private static final String selectByIdsAndSchema = "SELECT r FROM ReportPojo r" //
            + " WHERE r.id in :ids and r.schemaId = :id";

    private static final String selectByTypeAndName = "SELECT r FROM ReportPojo r" //
            + " WHERE r.type = :type and r.name = :name";

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaId){
        TypedQuery<ReportPojo> q = createJpqlQuery(selectByTypeAndSchema);
        q.setParameter("type", type);
        q.setParameter("schemaId", schemaId);
        return selectMultiple(q);
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaId){
        TypedQuery<ReportPojo> q = createJpqlQuery(selectByIdsAndSchema);
        q.setParameter("ids", ids);
        q.setParameter("schemaId", schemaId);
        return selectMultiple(q);
    }

    public ReportPojo getByTypeAndName(ReportType type, String name){
        TypedQuery<ReportPojo> q = createJpqlQuery(selectByTypeAndName);
        q.setParameter("type", type);
        q.setParameter("name", name);
        return selectSingleOrNull(q);
    }



}
