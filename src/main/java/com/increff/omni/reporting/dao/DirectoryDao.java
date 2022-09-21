package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DirectoryDao extends AbstractDao<DirectoryPojo> {
}
