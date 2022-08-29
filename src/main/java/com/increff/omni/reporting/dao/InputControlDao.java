package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class InputControlDao extends AbstractDao<InputControlPojo> {

    private static final String selectByIds = "SELECT i FROM InputControlPojo i" //
            + " WHERE i.id IN :ids";

    public List<InputControlPojo> selectMultiple(List<Integer> ids){
        TypedQuery<InputControlPojo> q = createJpqlQuery(selectByIds);
        q.setParameter("ids", ids);
        return selectMultiple(q);
    }

}
