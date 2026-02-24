package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ClickHouseDatabaseMappingPojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class ClickHouseDatabaseMappingDao extends AbstractDao<ClickHouseDatabaseMappingPojo> {

    private static final String SELECT_BY_CONNECTION_ID = "select p from ClickHouseDatabaseMappingPojo p where p.connectionId=:connectionId";

    public List<ClickHouseDatabaseMappingPojo> selectByConnectionId(Integer connectionId) {
        TypedQuery<ClickHouseDatabaseMappingPojo> q = createJpqlQuery(SELECT_BY_CONNECTION_ID);
        q.setParameter("connectionId", connectionId);
        return q.getResultList();
    }

}
