package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.commons.springboot.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ConnectionDao extends AbstractDao<ConnectionPojo> {

}
