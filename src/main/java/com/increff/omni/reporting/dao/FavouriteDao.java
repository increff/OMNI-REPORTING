package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.FavouritePojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Objects;

@Repository
@Transactional
public class FavouriteDao extends AbstractDao<FavouritePojo> {

    public FavouritePojo getByOrgUser(Integer orgId, Integer userId) {
        if (Objects.isNull(userId))
            return getByOrgUserNull(orgId);

        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<FavouritePojo> query = cb.createQuery(FavouritePojo.class);
        Root<FavouritePojo> root = query.from(FavouritePojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("orgId"), orgId),
                        cb.equal(root.get("userId"), userId)
                )
        );
        TypedQuery<FavouritePojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public FavouritePojo getByOrgUserNull(Integer orgId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<FavouritePojo> query = cb.createQuery(FavouritePojo.class);
        Root<FavouritePojo> root = query.from(FavouritePojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("orgId"), orgId),
                        cb.isNull(root.get("userId"))
                )
        );
        TypedQuery<FavouritePojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

}
