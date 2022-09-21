package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlQuery;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class InputControlQueryDao extends AbstractDao<InputControlQuery> {

    private static final String selectByIds = "SELECT i FROM InputControlQuery i" //
            + " WHERE i.controlId IN :controlIds";

    public List<InputControlQuery> selectMultiple(List<Integer> controlIds){
        TypedQuery<InputControlQuery> q = createJpqlQuery(selectByIds);
        q.setParameter("controlIds", controlIds);
        return selectMultiple(q);
    }

}
