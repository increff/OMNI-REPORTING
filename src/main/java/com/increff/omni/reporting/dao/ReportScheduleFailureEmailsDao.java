package com.increff.omni.reporting.dao;

import com.increff.commons.springboot.db.dao.AbstractDao;
import com.increff.omni.reporting.pojo.ReportScheduleFailureEmailPojo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReportScheduleFailureEmailsDao extends AbstractDao<ReportScheduleFailureEmailPojo> {
}
