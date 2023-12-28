package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.commons.springboot.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class InputControlQueryDao extends AbstractDao<InputControlQueryPojo> {

    public List<InputControlQueryPojo> selectMultiple(List<Integer> controlIds){
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlQueryPojo> query = cb.createQuery(InputControlQueryPojo.class);
        Root<InputControlQueryPojo> root = query.from(InputControlQueryPojo.class);
        query.where(
                root.get("controlId").in(controlIds)
        );
        TypedQuery<InputControlQueryPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

}
