package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.SchemaDao;
import com.increff.omni.reporting.pojo.SchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class SchemaApi extends AbstractApi {

    @Autowired
    private SchemaDao dao;

    public SchemaPojo getCheck(Integer id) throws ApiException {
        SchemaPojo pojo = dao.select(id);
        checkNotNull(pojo, "No schema present with id : " + id);
        return pojo;
    }

    public SchemaPojo add(SchemaPojo pojo) throws ApiException{
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public List<SchemaPojo> selectAll(){
        return dao.selectAll();
    }


    private void validate(SchemaPojo pojo) throws ApiException {
        SchemaPojo existing = dao.select("name", pojo.getName());
        checkNotNull(existing, "Schema already present with name : " + pojo.getName());
    }


}
