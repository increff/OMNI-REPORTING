package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class InputControlValuesDao extends AbstractDao<InputControlValuesPojo> {

    private static final String selectByIds = "SELECT i FROM InputControlValuesPojo i" //
            + " WHERE i.controlId IN :controlIds";

    public List<InputControlValuesPojo> selectMultiple(List<Integer> controlIds){
        TypedQuery<InputControlValuesPojo> q = createJpqlQuery(selectByIds);
        q.setParameter("controlIds", controlIds);
        return selectMultiple(q);
    }

}
