package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.OrganizationPojo;
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
