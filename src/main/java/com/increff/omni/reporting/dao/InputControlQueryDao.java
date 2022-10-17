package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class InputControlQueryDao extends AbstractDao<InputControlQueryPojo> {

    private static final String selectByIds = "SELECT i FROM InputControlQueryPojo i" //
            + " WHERE i.controlId IN :controlIds";

    public List<InputControlQueryPojo> selectMultiple(List<Integer> controlIds){
        TypedQuery<InputControlQueryPojo> q = createJpqlQuery(selectByIds);
        q.setParameter("controlIds", controlIds);
        return selectMultiple(q);
    }

}
