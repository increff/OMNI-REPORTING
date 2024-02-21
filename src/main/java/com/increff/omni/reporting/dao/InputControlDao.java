package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;

@Repository
@Transactional
public class InputControlDao extends AbstractDao<InputControlPojo> {

    public List<InputControlPojo> selectMultiple(List<Integer> ids) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlPojo> query = cb.createQuery(InputControlPojo.class);
        Root<InputControlPojo> root = query.from(InputControlPojo.class);
        query.where(
                root.get("id").in(ids)
        );
        TypedQuery<InputControlPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public InputControlPojo selectByScopeAndDisplayName(InputControlScope scope, String displayName,
                                                        Integer schemaVersionId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlPojo> query = cb.createQuery(InputControlPojo.class);
        Root<InputControlPojo> root = query.from(InputControlPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("scope"), scope),
                        cb.equal(root.get("displayName"), displayName),
                        Objects.isNull(schemaVersionId) ? // todo : ques cb.equal(root.get) does not work for null values. ask if this way is correct and add in other places for input control dao and report dao
                                cb.isNull(root.get("schemaVersionId")) : cb.equal(root.get("schemaVersionId"), schemaVersionId)
                )
        );
        TypedQuery<InputControlPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public InputControlPojo selectByScopeAndParamName(InputControlScope scope, String paramName,
                                                      Integer schemaVersionId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlPojo> query = cb.createQuery(InputControlPojo.class);
        Root<InputControlPojo> root = query.from(InputControlPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("scope"), scope),
                        cb.equal(root.get("paramName"), paramName),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId)
                )
        );
        TypedQuery<InputControlPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<InputControlPojo> selectByScopeAndSchemaVersion(InputControlScope scope, Integer schemaVersionId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlPojo> query = cb.createQuery(InputControlPojo.class);
        Root<InputControlPojo> root = query.from(InputControlPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("scope"), scope),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId)
                )
        );
        TypedQuery<InputControlPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<InputControlPojo> selectBySchemaVersion(Integer schemaVersionId) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<InputControlPojo> query = cb.createQuery(InputControlPojo.class);
        Root<InputControlPojo> root = query.from(InputControlPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("schemaVersionId"), schemaVersionId)
                )
        );
        TypedQuery<InputControlPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }
}
