package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.OrganizationPojo;
//import com.nextscm.commons.spring.db.AbstractDao;
import com.increff.omni.reporting.commons.AbstractDao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Root;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Repository
@Transactional
public class OrganizationDao extends AbstractDao<OrganizationPojo> {

    public List<OrganizationPojo> selectByIds(List<Integer> ids) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<OrganizationPojo> query = cb.createQuery(OrganizationPojo.class);
        Root<OrganizationPojo> root = query.from(OrganizationPojo.class);
        query.where(
                root.get("id").in(ids)
        );
        TypedQuery<OrganizationPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
