package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.SchedulePipelinePojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
@Transactional
public class SchedulePipelineDao extends AbstractDao<SchedulePipelinePojo> {

}
