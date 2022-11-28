package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class InputControlValuesDao extends AbstractDao<InputControlValuesPojo> {

    public List<InputControlValuesPojo> selectMultiple(List<Integer> controlIds){
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlValuesPojo> query = cb.createQuery(InputControlValuesPojo.class);
        Root<InputControlValuesPojo> root = query.from(InputControlValuesPojo.class);
        query.where(
                root.get("controlId").in(controlIds)
        );
        TypedQuery<InputControlValuesPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

}
