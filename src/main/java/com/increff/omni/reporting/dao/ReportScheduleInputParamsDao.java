package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ReportScheduleInputParamsPojo;
//import com.nextscm.commons.spring.db.AbstractDao;
import com.increff.omni.reporting.commons.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReportScheduleInputParamsDao extends AbstractDao<ReportScheduleInputParamsPojo> {
}
