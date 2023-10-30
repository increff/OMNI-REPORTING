package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.DashboardChartPojo;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public class DashboardDao extends AbstractDao<DashboardPojo> {

    public DashboardPojo getCheck(Integer id) {
        return em().find(DashboardPojo.class, id);
    }
}
