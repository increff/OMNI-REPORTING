package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.OrgSchemaVersionPojo;
import com.increff.commons.springboot.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class OrgSchemaDao extends AbstractDao<OrgSchemaVersionPojo> {
}
