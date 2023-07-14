package com.nextscm.commons.spring.audit.dao;

import com.increff.omni.reporting.commons.AbstractDao;
import com.nextscm.commons.spring.audit.pojo.AuditPojo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public class AuditDao extends AbstractDao<AuditPojo> implements DaoProvider{

    @Transactional
    @Override
    public List<AuditPojo> getAuditLogsByTime(Date startTimestamp, Date endTimestamp) {

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<AuditPojo> criteriaQuery = criteriaBuilder.createQuery(AuditPojo.class);
        Root<AuditPojo> root = criteriaQuery.from(AuditPojo.class);
        criteriaQuery.select(root);
        criteriaQuery
                .where(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startTimestamp),
                        criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endTimestamp)));
        return em.createQuery(criteriaQuery).getResultList();
    }

    @Transactional
    @Override
    public void save(AuditPojo auditPojo) {
        em.persist(auditPojo);
    }

    @Transactional
    @Override
    public List<AuditPojo> selectByIdAndType(String objectId, String objectType) {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        CriteriaQuery<AuditPojo> criteriaQuery = criteriaBuilder.createQuery(AuditPojo.class);
        Root<AuditPojo> root = criteriaQuery.from(AuditPojo.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(root.get("objectId"), objectId),
                criteriaBuilder.equal(root.get("objectType"), objectType)));
        return em.createQuery(criteriaQuery).getResultList();
    }

    @Transactional
    @Override
    public List<AuditPojo> selectMultiple(String member, Object value) {
        return selectMultiple(member, value);
    }
}
