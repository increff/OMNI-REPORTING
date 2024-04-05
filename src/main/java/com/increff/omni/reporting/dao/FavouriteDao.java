package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.FavouritePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.util.Objects;

@Repository
@Transactional
public class FavouriteDao extends AbstractDao<FavouritePojo> {

    private static final String SELECT_BY_ORG_USER = "SELECT o FROM FavouritePojo o WHERE o.orgId=:orgId AND o.userId=:userId";
    private static final String SELECT_BY_ORG_USER_NULL = "SELECT o FROM FavouritePojo o WHERE o.orgId=:orgId AND o.userId IS NULL";

    public FavouritePojo getByOrgUser(Integer orgId, Integer userId) {
        if (Objects.isNull(userId))
            return getByOrgUserNull(orgId);
        TypedQuery<FavouritePojo> q = createJpqlQuery(SELECT_BY_ORG_USER);
        q.setParameter("orgId", orgId);
        q.setParameter("userId", userId);
        return selectSingleOrNull(q);
    }

    public FavouritePojo getByOrgUserNull(Integer orgId) {
        TypedQuery<FavouritePojo> q = createJpqlQuery(SELECT_BY_ORG_USER_NULL);
        q.setParameter("orgId", orgId);
        return selectSingleOrNull(q);
    }

}
