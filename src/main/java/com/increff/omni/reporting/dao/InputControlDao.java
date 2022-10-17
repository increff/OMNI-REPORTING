package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@Transactional
public class InputControlDao extends AbstractDao<InputControlPojo> {

    private static final String selectByIds = "SELECT i FROM InputControlPojo i" //
            + " WHERE i.id IN :ids";
    private static final String selectByScopeAndDisplayName = "SELECT i FROM InputControlPojo i" //
            + " WHERE i.scope = :scope AND i.displayName = :displayName";
    private static final String selectByScopeAndParamName = "SELECT i FROM InputControlPojo i" //
            + " WHERE i.scope = :scope AND i.paramName = :paramName";

    public List<InputControlPojo> selectMultiple(List<Integer> ids){
        TypedQuery<InputControlPojo> q = createJpqlQuery(selectByIds);
        q.setParameter("ids", ids);
        return selectMultiple(q);
    }

    public InputControlPojo selectByScopeAndDisplayName(InputControlScope scope, String displayName){
        TypedQuery<InputControlPojo> q = createJpqlQuery(selectByScopeAndDisplayName);
        q.setParameter("scope", scope);
        q.setParameter("displayName", displayName);
        return selectSingleOrNull(q);
    }

    public InputControlPojo selectByScopeAndParamName(InputControlScope scope, String paramName){
        TypedQuery<InputControlPojo> q = createJpqlQuery(selectByScopeAndParamName);
        q.setParameter("scope", scope);
        q.setParameter("paramName", paramName);
        return selectSingleOrNull(q);
    }

}
